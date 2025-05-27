package it.finanze.sanita.fse2.ms.edsclient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import it.finanze.sanita.fse2.ms.edsclient.client.impl.BrokerClient;
import it.finanze.sanita.fse2.ms.edsclient.config.Constants;
import it.finanze.sanita.fse2.ms.edsclient.dto.EdsResponseDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.PublicationRequestBodyDTO;
import it.finanze.sanita.fse2.ms.edsclient.enums.DestinationEnum;
import it.finanze.sanita.fse2.ms.edsclient.repository.entity.IniEdsInvocationETY;
import it.finanze.sanita.fse2.ms.edsclient.repository.impl.EdsInvocationRepo;
import it.finanze.sanita.fse2.ms.edsclient.service.impl.ConfigSRV;
import it.finanze.sanita.fse2.ms.edsclient.service.impl.EdsInvocationSRV;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = { Constants.ComponentScan.BASE })
@ActiveProfiles(Constants.Profile.TEST)
public class EdsInvocationSRVTest {

    @Autowired
    private EdsInvocationSRV edsInvocationSRV;

    @MockitoBean
    private BrokerClient brokerClient;

    @MockitoBean
    private EdsInvocationRepo edsInvocationRepo;

    @MockitoBean
    private ConfigSRV configSRV;

    @Test
    void testReplaceByWorkflowInstanceIdAndIdentifier() {
        EdsResponseDTO out = new EdsResponseDTO();
        IniEdsInvocationETY iniEdsInvocationETY = new IniEdsInvocationETY();
        String workFlowInstanceId = "test";
        String identifier = "test";
        iniEdsInvocationETY.setWorkflowInstanceId("test");
        iniEdsInvocationETY.setData(new Document("key", "test"));
        out.setEsito(true);
        DestinationEnum enums = DestinationEnum.SEND_TO_UAR;
        when(brokerClient.dispatchAndSendData(Mockito.any(), enums)).thenReturn(out);
        when(edsInvocationRepo.find(Mockito.anyString())).thenReturn(iniEdsInvocationETY);
        when(configSRV.isRemoveMetadataEnable()).thenReturn(true);
        edsInvocationSRV.replace(identifier, workFlowInstanceId, enums);

        verify(edsInvocationRepo, times(1)).remove(Mockito.anyString());
    }

    @Test
    void testPublishByWorkflowInstanceIdAndPriorityDocumentNotFound() {
        EdsResponseDTO out;
        String workFlowInstanceId = "test";
        PublicationRequestBodyDTO request = new PublicationRequestBodyDTO();
        request.setWorkflowInstanceId(workFlowInstanceId);

        when(edsInvocationRepo.find(Mockito.anyString())).thenReturn(null);
        out = edsInvocationSRV.publish(request.getIdentificativoDoc(), request.getWorkflowInstanceId(),
                DestinationEnum.fromString(request.getDestination()));

        assertEquals(out.getMessageError(),
                "Nessun documento trovato per il workflowInstanceId: " + request.getWorkflowInstanceId());
    }

}
