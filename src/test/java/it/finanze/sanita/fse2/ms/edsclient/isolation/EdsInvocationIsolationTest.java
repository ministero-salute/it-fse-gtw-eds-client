package it.finanze.sanita.fse2.ms.edsclient.isolation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.bson.Document;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import it.finanze.sanita.fse2.ms.edsclient.client.IBrokerClient;
import it.finanze.sanita.fse2.ms.edsclient.dto.EdsResponseDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.BrokerRequestDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.EdsMetadataUpdateReqDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.response.GetDocumentReferenceResDTO;
import it.finanze.sanita.fse2.ms.edsclient.enums.ProcessorOperationEnum;
import it.finanze.sanita.fse2.ms.edsclient.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.edsclient.repository.IEdsInvocationRepo;
import it.finanze.sanita.fse2.ms.edsclient.repository.entity.IniEdsInvocationETY;
import it.finanze.sanita.fse2.ms.edsclient.service.IConfigSRV;
import it.finanze.sanita.fse2.ms.edsclient.service.impl.EdsInvocationSRV;

@Tag("ISOLATION")
@ActiveProfiles("ISOLATION")
@SpringJUnitConfig(EdsInvocationSRV.class)
class EdsInvocationIsolationTest {
    @Autowired private EdsInvocationSRV service;
    @MockitoBean private IEdsInvocationRepo repository;
    @MockitoBean private IConfigSRV config;
    @MockitoBean private IBrokerClient broker;

    private EdsResponseDTO invoke(boolean replace) {
        return replace ? service.replace("doc", "workflow") : service.publish("doc", "workflow");
    }

    private IniEdsInvocationETY document() {
        IniEdsInvocationETY document = new IniEdsInvocationETY();
        document.setData(new Document("resourceType", "Bundle"));
        document.setFiscalCode("fiscal-code");
        return document;
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void missingDocumentsNeverReachBroker(boolean replace) {
        assertFalse(invoke(replace).isEsito());
        assertTrue(invoke(replace).getMessageError().contains("workflow"));
        when(repository.find("workflow")).thenReturn(new IniEdsInvocationETY());
        assertFalse(invoke(replace).isEsito());
        verifyNoInteractions(broker, config);
        verify(repository, never()).remove(any());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void dispatchesRealRequestAndRemovesOnlySuccessfulDocumentsWhenEnabled(boolean replace) {
        IniEdsInvocationETY document = document();
        when(repository.find("workflow")).thenReturn(document);
        EdsResponseDTO response = new EdsResponseDTO();
        response.setEsito(true);
        when(broker.dispatchAndSendData(any())).thenReturn(response);
        when(config.isRemoveMetadataEnable()).thenReturn(true);
        assertSame(response, invoke(replace));
        ArgumentCaptor<BrokerRequestDTO> request = ArgumentCaptor.forClass(BrokerRequestDTO.class);
        verify(broker).dispatchAndSendData(request.capture());
        assertEquals(replace ? ProcessorOperationEnum.REPLACE : ProcessorOperationEnum.PUBLISH,
                request.getValue().getOperation());
        assertSame(document, request.getValue().getIniEdsInvocationETY());
        assertEquals("doc", request.getValue().getIdentifier());
        assertEquals("workflow", request.getValue().getWorkflowInstanceId());
        assertEquals("fiscal-code", request.getValue().getOptionalLogData().getFiscalCode());
        verify(repository).remove("workflow");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void retainsMetadataForDisabledCleanupFailedOrNullResponse(boolean replace) {
        when(repository.find("workflow")).thenReturn(document());
        EdsResponseDTO response = new EdsResponseDTO();
        response.setEsito(true);
        when(broker.dispatchAndSendData(any())).thenReturn(response);
        when(config.isRemoveMetadataEnable()).thenReturn(false);
        assertSame(response, invoke(replace));
        response.setEsito(false);
        assertSame(response, invoke(replace));
        when(broker.dispatchAndSendData(any())).thenReturn(null);
        assertNull(invoke(replace));
        verify(repository, never()).remove(any());
    }

    @Test
    void publishConvertsNestedFailureButReplacePropagatesIt() {
        when(repository.find("workflow")).thenReturn(document());
        IllegalArgumentException cause = new IllegalArgumentException("invalid payload");
        RuntimeException failure = new RuntimeException("broker failed", cause);
        when(broker.dispatchAndSendData(any())).thenThrow(failure);
        EdsResponseDTO response = service.publish("doc", "workflow");
        assertEquals("broker failed", response.getMessageError());
        assertEquals(IllegalArgumentException.class.getCanonicalName(), response.getExClassCanonicalName());
        assertSame(failure, assertThrows(RuntimeException.class, () -> service.replace("doc", "workflow")));
        verify(repository, never()).remove(any());
    }

    @Test
    void forwardsDeleteUpdateAndReferenceArguments() {
        EdsResponseDTO response = new EdsResponseDTO();
        when(broker.dispatchAndSendData(any())).thenReturn(response);
        assertSame(response, service.delete("doc", "fiscal-code", "jwt"));
        EdsMetadataUpdateReqDTO update = new EdsMetadataUpdateReqDTO("doc", "workflow", "{}", "fiscal-code");
        assertSame(response, service.update("doc", update, "fiscal-code", "jwt"));
        ArgumentCaptor<BrokerRequestDTO> request = ArgumentCaptor.forClass(BrokerRequestDTO.class);
        verify(broker, times(2)).dispatchAndSendData(request.capture());
        assertEquals(ProcessorOperationEnum.DELETE, request.getAllValues().getFirst().getOperation());
        assertEquals(ProcessorOperationEnum.UPDATE, request.getValue().getOperation());
        assertSame(update, request.getValue().getUpdateReqDTO());
        assertEquals("jwt", request.getValue().getJwt());
        assertEquals("fiscal-code", request.getValue().getFiscalCode());
        GetDocumentReferenceResDTO reference = new GetDocumentReferenceResDTO();
        when(broker.getDocumentReference("fiscal-code", "doc", "jwt")).thenReturn(reference);
        assertSame(reference, service.getDocumentReference("doc", "fiscal-code", "jwt"));
    }

    @Test
    void wrapsDeleteFailureAndPropagatesRepositoryFailure() {
        RuntimeException failure = new RuntimeException("unavailable");
        when(broker.dispatchAndSendData(any())).thenThrow(failure);
        assertSame(failure, assertThrows(BusinessException.class,
                () -> service.delete("doc", "fiscal-code", "jwt")).getCause());
        when(repository.find("workflow")).thenThrow(failure);
        assertSame(failure, assertThrows(RuntimeException.class, () -> service.publish("doc", "workflow")));
    }
}
