package it.finanze.sanita.fse2.ms.edsclient.isolation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.List;
import org.bson.Document;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.client.RestTemplate;

import it.finanze.sanita.fse2.ms.edsclient.client.impl.BrokerClient;
import it.finanze.sanita.fse2.ms.edsclient.config.BrokerCfg;
import it.finanze.sanita.fse2.ms.edsclient.config.Constants;
import it.finanze.sanita.fse2.ms.edsclient.dto.*;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.*;
import it.finanze.sanita.fse2.ms.edsclient.dto.response.*;
import it.finanze.sanita.fse2.ms.edsclient.enums.ProcessorOperationEnum;
import it.finanze.sanita.fse2.ms.edsclient.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.edsclient.logging.LoggerHelper;
import it.finanze.sanita.fse2.ms.edsclient.repository.entity.IniEdsInvocationETY;
import it.finanze.sanita.fse2.ms.edsclient.utility.JwtUtility;

@Tag("ISOLATION")
@ActiveProfiles("ISOLATION")
@SpringJUnitConfig({BrokerClient.class, BrokerCfg.class})
@TestPropertySource(properties = "srv-broker.url.host=http://broker.invalid/")
class BrokerClientIsolationTest {
    @Autowired private BrokerClient client;
    @MockitoBean private RestTemplate http;
    @MockitoBean private LoggerHelper logger;
    @MockitoBean private JwtUtility jwt;

    private BrokerRequestDTO request(ProcessorOperationEnum operation) {
        IniEdsInvocationETY document = new IniEdsInvocationETY();
        document.setData(new Document("resourceType", "Bundle"));
        document.setFiscalCode("fiscal");
        document.setRde("region");
        document.setMetadata(List.of(new Document("tokenEntry", new Document("payload", new Document("iss", "issuer")))));
        EdsMetadataUpdateReqDTO update = new EdsMetadataUpdateReqDTO("doc", "workflow", "{}", "fiscal");
        update.setDocumentReference("{\"resourceType\":\"DocumentReference\"}");
        return BrokerRequestDTO.builder().operation(operation).identifier("doc").workflowInstanceId("workflow")
                .fiscalCode("fiscal").jwt("original-token").iniEdsInvocationETY(document).updateReqDTO(update).build();
    }

    @ParameterizedTest
    @EnumSource(ProcessorOperationEnum.class)
    void buildsMethodPathBodyAndAuthenticatedHeader(ProcessorOperationEnum operation) {
        when(jwt.reSignToken("original-token")).thenReturn("resigned-token");
        when(jwt.generateToken(anyMap())).thenReturn("generated-token");
        BrokerRequestDTO request = request(operation);
        assertTrue(client.dispatchAndSendData(request).isEsito());
        ArgumentCaptor<URI> uri = ArgumentCaptor.forClass(URI.class);
        ArgumentCaptor<HttpEntity<?>> entity = ArgumentCaptor.forClass(HttpEntity.class);
        verify(http).exchange(uri.capture(), eq(Constants.AppConstants.methodMap.get(operation)),
                entity.capture(), eq(DocumentResponseDTO.class));
        String suffix = switch (operation) {
            case DELETE -> "/identifier/doc/fiscal";
            case UPDATE -> "/metadata";
            default -> "/workflowinstanceid/workflow";
        };
        assertTrue(uri.getValue().getPath().endsWith("/edsalim/v1/ingestion/document" + suffix));
        assertEquals(MediaType.APPLICATION_JSON, entity.getValue().getHeaders().getContentType());
        assertEquals(operation == ProcessorOperationEnum.DELETE || operation == ProcessorOperationEnum.UPDATE
                ? "resigned-token" : "generated-token", entity.getValue().getHeaders().getFirst("Agid-JWT-Signature"));
        if (operation == ProcessorOperationEnum.DELETE) {
            assertNull(entity.getValue().getBody());
        } else {
            DocumentDTO body = (DocumentDTO) entity.getValue().getBody();
            assertEquals("doc", body.getIdentifier());
            assertEquals("fiscal", body.getFiscalCode());
            assertEquals(operation, body.getOperation());
            assertTrue(body.getJsonString().contains(operation == ProcessorOperationEnum.UPDATE ? "DocumentReference" : "Bundle"));
        }
        verify(logger).info(anyString(), any(), any(), any(), any());
    }

    @ParameterizedTest
    @EnumSource(ProcessorOperationEnum.class)
    void reportsTransportFailureWithoutThrowingEvenIfErrorLoggingFails(ProcessorOperationEnum operation) {
        when(http.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(DocumentResponseDTO.class)))
                .thenThrow(new RuntimeException("transport failed", new IllegalStateException("offline")));
        doThrow(new IllegalStateException("logger failed")).when(logger).error(anyString(), any(), any(), any(), any(), any());
        EdsResponseDTO result = client.dispatchAndSendData(request(operation));
        assertFalse(result.isEsito());
        assertEquals("transport failed", result.getMessageError());
        assertEquals(IllegalStateException.class.getCanonicalName(), result.getExClassCanonicalName());
    }

    @ParameterizedTest
    @EnumSource(value = ProcessorOperationEnum.class, names = {"PUBLISH", "REPLACE", "UPDATE"})
    void rejectsMissingPayloadBeforeTransport(ProcessorOperationEnum operation) {
        BrokerRequestDTO request = request(operation);
        request.getIniEdsInvocationETY().setData(null);
        request.setUpdateReqDTO(null);
        EdsResponseDTO result = client.dispatchAndSendData(request);
        assertFalse(result.isEsito());
        assertEquals("Unsupported exception", result.getMessageError());
        verifyNoInteractions(http);
    }

    @Test
    void missingJwtMetadataAndBlankTokensOmitAuthentication() {
        BrokerRequestDTO request = request(ProcessorOperationEnum.DELETE);
        request.setJwt(null);
        assertTrue(client.dispatchAndSendData(request).isEsito());
        request.setJwt(" ");
        assertTrue(client.dispatchAndSendData(request).isEsito());
        request = request(ProcessorOperationEnum.PUBLISH);
        request.getIniEdsInvocationETY().setMetadata(null);
        assertTrue(client.dispatchAndSendData(request).isEsito());
        request.getIniEdsInvocationETY().setMetadata(List.of(new Document()));
        assertTrue(client.dispatchAndSendData(request).isEsito());
        ArgumentCaptor<HttpEntity<?>> entity = ArgumentCaptor.forClass(HttpEntity.class);
        verify(http, times(4)).exchange(any(URI.class), any(HttpMethod.class), entity.capture(), eq(DocumentResponseDTO.class));
        entity.getAllValues().forEach(value -> assertFalse(value.getHeaders().containsKey("Agid-JWT-Signature")));
        verifyNoInteractions(jwt);
    }

    @Test
    void successfulDispatchSurvivesLoggingFailure() {
        doThrow(new IllegalStateException("logger unavailable")).when(logger).info(anyString(), any(), any(), any(), any());
        assertTrue(client.dispatchAndSendData(request(ProcessorOperationEnum.DELETE)).isEsito());
    }

    @Test
    void retrievesReferenceUsingResignedTokenAndRejectsMissingToken() {
        GetDocumentReferenceResDTO reference = new GetDocumentReferenceResDTO();
        when(jwt.reSignToken("jwt")).thenReturn("resigned");
        when(http.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(GetDocumentReferenceResDTO.class)))
                .thenReturn(ResponseEntity.ok(reference));
        assertSame(reference, client.getDocumentReference("fiscal", "doc", "jwt"));
        ArgumentCaptor<HttpEntity<?>> entity = ArgumentCaptor.forClass(HttpEntity.class);
        verify(http).exchange(eq(URI.create("http://broker.invalid/edsalim/v1/ingestion/document-reference/fiscal/doc")),
                eq(HttpMethod.GET), entity.capture(), eq(GetDocumentReferenceResDTO.class));
        assertEquals("resigned", entity.getValue().getHeaders().getFirst("Agid-JWT-Signature"));
        assertThrows(BusinessException.class, () -> client.getDocumentReference("fiscal", "doc", null));
        assertThrows(BusinessException.class, () -> client.getDocumentReference("fiscal", "doc", " "));
    }

    @Test
    void retrievesStatusAndWrapsTransportFailure() {
        GetIngestionStatusResponseDTO response = new GetIngestionStatusResponseDTO();
        when(http.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(GetIngestionStatusResponseDTO.class)))
                .thenReturn(ResponseEntity.ok(response));
        assertSame(response, client.getIngestionStatus("workflow"));
        RuntimeException failure = new RuntimeException("offline");
        when(http.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(GetIngestionStatusResponseDTO.class)))
                .thenThrow(failure);
        assertSame(failure, assertThrows(BusinessException.class, () -> client.getIngestionStatus("workflow")).getCause());
    }
}
