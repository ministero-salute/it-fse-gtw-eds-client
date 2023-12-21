package it.finanze.sanita.fse2.ms.edsclient;

import it.finanze.sanita.fse2.ms.edsclient.client.impl.EdsClient;
import it.finanze.sanita.fse2.ms.edsclient.config.Constants;
import it.finanze.sanita.fse2.ms.edsclient.dto.EdsResponseDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.PublicationRequestBodyDTO;
import it.finanze.sanita.fse2.ms.edsclient.repository.entity.IniEdsInvocationETY;
import it.finanze.sanita.fse2.ms.edsclient.repository.impl.EdsInvocationRepo;
import it.finanze.sanita.fse2.ms.edsclient.service.impl.ConfigSRV;
import it.finanze.sanita.fse2.ms.edsclient.service.impl.EdsInvocationSRV;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = {Constants.ComponentScan.BASE})
@ActiveProfiles(Constants.Profile.TEST)
public class EdsInvocationSRVTest {

    @Autowired
    private EdsInvocationSRV edsInvocationSRV;

    @MockBean
    private EdsClient edsClient;

    @MockBean
    private EdsInvocationRepo edsInvocationRepo;

    @MockBean
    private ConfigSRV configSRV;

    @Test
    void testReplaceByWorkflowInstanceIdAndIdentifier(){
        EdsResponseDTO out = new EdsResponseDTO();
        IniEdsInvocationETY iniEdsInvocationETY = new IniEdsInvocationETY();
        String workFlowInstanceId = "test";
        String identifier = "test";
        iniEdsInvocationETY.setWorkflowInstanceId("test");
        iniEdsInvocationETY.setData(new Document("key", "test"));
        out.setEsito(true);

        when(edsClient.dispatchAndSendData(Mockito.any())).thenReturn(out);
        when(edsInvocationRepo.findByWorkflowInstanceId(Mockito.anyString())).thenReturn(iniEdsInvocationETY);
        when(configSRV.isRemoveMetadataEnable()).thenReturn(true);
        edsInvocationSRV.replaceByWorkflowInstanceIdAndIdentifier(identifier, workFlowInstanceId);

        verify(edsInvocationRepo, times(1)).removeByWorkflowInstanceId(Mockito.anyString());
    }

    @Test
    void testPublishByWorkflowInstanceIdAndPriorityDocumentNotFound(){
        EdsResponseDTO out;
        String workFlowInstanceId = "test";
        PublicationRequestBodyDTO request = new PublicationRequestBodyDTO();
        request.setWorkflowInstanceId(workFlowInstanceId);

        when(edsInvocationRepo.findByWorkflowInstanceId(Mockito.anyString())).thenReturn(null);
        out = edsInvocationSRV.publishByWorkflowInstanceIdAndPriority(request);


        assertEquals(out.getMessageError(), "Nessun documento trovato per il workflowInstanceId: " + request.getWorkflowInstanceId());
    }

}
