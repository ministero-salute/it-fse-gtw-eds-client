package it.finanze.sanita.fse2.ms.edsclient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpServerErrorException;

import it.finanze.sanita.fse2.ms.edsclient.client.IEdsClient;
import it.finanze.sanita.fse2.ms.edsclient.config.Constants;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.EdsMetadataUpdateReqDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.PublicationMetadataReqDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.PublicationRequestBodyDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.response.EDSPublicationResponseDTO;
import it.finanze.sanita.fse2.ms.edsclient.enums.ProcessorOperationEnum;
import it.finanze.sanita.fse2.ms.edsclient.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.edsclient.repository.entity.IniEdsInvocationETY;
import it.finanze.sanita.fse2.ms.edsclient.service.IEdsInvocationSRV;
import it.finanze.sanita.fse2.ms.edsclient.utility.JsonUtility;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = {Constants.ComponentScan.BASE})
@ActiveProfiles(Constants.Profile.TEST)
class EdsClientTest extends AbstractTest {

    @Autowired
    MongoTemplate mongoTemplate;

    @MockBean
    IEdsInvocationSRV edsInvocationSRV;
    
    @MockBean
    IEdsClient edsClient;

    String TEST_WORKFLOW_INSTANCE_ID = UUID.randomUUID().toString(); 
    
    @BeforeEach
    void setup() {
        mongoTemplate.dropCollection(IniEdsInvocationETY.class);
    }

    @Test
    void publishTest() {

        final String workflowInstanceId = TEST_WORKFLOW_INSTANCE_ID;
        final IniEdsInvocationETY invocation = insertIniEdsInvocation(workflowInstanceId);

        assertNotNull(invocation, "Invocation must exist to test the client call");
        mockEdsClient(true, ProcessorOperationEnum.PUBLISH);

        final ResponseEntity<EDSPublicationResponseDTO> response = callPublishEdsClient(workflowInstanceId);

        assertNotNull(response, "A response should be returned");

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Calling the client should return 200 OK");
        assertTrue(response.getBody().getEsito(), "The mocked outcome should be true");
        assertNotNull(response.getBody().getSpanID(), "The span id should be returned");
        assertNotNull(response.getBody().getTraceID(), "The trace id should be returned");
    } 

    @Test
    void updateTest() {

        final String workflowInstanceId = TEST_WORKFLOW_INSTANCE_ID; 
        final String docId = UUID.randomUUID().toString();

        String req = "{\"tipologiaStruttura\":\"Ospedale\",\"attiCliniciRegoleAccesso\":[\"P99\"],\"tipoDocumentoLivAlto\":\"WOR\",\"assettoOrganizzativo\":\"AD_PSC001\",\"dataInizioPrestazione\":\"1661246473\",\"dataFinePrestazione\":\"1661246473\",\"conservazioneANorma\":\"string\",\"tipoAttivitaClinica\":\"PHR\",\"identificativoSottomissione\":\"2.16.840.1.113883.2.9.2.90.4.4^090A02205783394_PRESPEC\"}";
        PublicationMetadataReqDTO publicationMetadataReqDTO = JsonUtility.jsonToObject(req, PublicationMetadataReqDTO.class);
        mockEdsClient(true, ProcessorOperationEnum.UPDATE);

        final ResponseEntity<EDSPublicationResponseDTO> response = callUpdateEdsClient(docId, workflowInstanceId, publicationMetadataReqDTO);

        assertNotNull(response, "A response should be returned");

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Calling the client should return 200 OK");
        assertTrue(response.getBody().getEsito(), "The mocked outcome should be true");
        assertNotNull(response.getBody().getSpanID(), "The span id should be returned");
        assertNotNull(response.getBody().getTraceID(), "The trace id should be returned");
    } 
    
    @Test
    void updateThrowsTest() {

        final String workflowInstanceId = TEST_WORKFLOW_INSTANCE_ID; 
        final String docId = UUID.randomUUID().toString();

        String req = "{\"tipologiaStruttura\":\"Ospedale\",\"attiCliniciRegoleAccesso\":[\"P99\"],\"tipoDocumentoLivAlto\":\"WOR\",\"assettoOrganizzativo\":\"AD_PSC001\",\"dataInizioPrestazione\":\"1661246473\",\"dataFinePrestazione\":\"1661246473\",\"conservazioneANorma\":\"string\",\"tipoAttivitaClinica\":\"PHR\",\"identificativoSottomissione\":\"2.16.840.1.113883.2.9.2.90.4.4^090A02205783394_PRESPEC\"}";
        PublicationMetadataReqDTO publicationMetadataReqDTO = JsonUtility.jsonToObject(req, PublicationMetadataReqDTO.class);
        mockEdsClient(false, ProcessorOperationEnum.UPDATE);

        assertThrows(HttpServerErrorException.class, () -> callUpdateEdsClient(docId, workflowInstanceId, publicationMetadataReqDTO)); 
    } 
    
    @Test
    void replaceTest() {

        final String workflowInstanceId = TEST_WORKFLOW_INSTANCE_ID; 
        final String docId = UUID.randomUUID().toString();

        String req = "{\"tipologiaStruttura\":\"Ospedale\",\"attiCliniciRegoleAccesso\":[\"P99\"],\"tipoDocumentoLivAlto\":\"WOR\",\"assettoOrganizzativo\":\"AD_PSC001\",\"dataInizioPrestazione\":\"1661246473\",\"dataFinePrestazione\":\"1661246473\",\"conservazioneANorma\":\"string\",\"tipoAttivitaClinica\":\"PHR\",\"identificativoSottomissione\":\"2.16.840.1.113883.2.9.2.90.4.4^090A02205783394_PRESPEC\"}";
        JsonUtility.jsonToObject(req, PublicationMetadataReqDTO.class);
        mockEdsClient(true, ProcessorOperationEnum.REPLACE);

        final ResponseEntity<EDSPublicationResponseDTO> response = callReplaceEdsClient(docId, workflowInstanceId); 

        assertNotNull(response, "A response should be returned");

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Calling the client should return 200 OK");
        assertTrue(response.getBody().getEsito(), "The mocked outcome should be true");
        assertNotNull(response.getBody().getSpanID(), "The span id should be returned");
        assertNotNull(response.getBody().getTraceID(), "The trace id should be returned");
    } 
    
    @Test
    void deleteTest() {

    	final String docId = UUID.randomUUID().toString();
        mockEdsClient(true, ProcessorOperationEnum.DELETE); 
        
        final ResponseEntity<EDSPublicationResponseDTO> response = callDeleteEdsClient(docId); 

        assertNotNull(response, "A response should be returned");

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Calling the client should return 200 OK");
        assertTrue(response.getBody().getEsito(), "The mocked outcome should be true");
        assertNotNull(response.getBody().getSpanID(), "The span id should be returned");
        assertNotNull(response.getBody().getTraceID(), "The trace id should be returned");
    } 

    @Test
    void publishTestRealClient() {

        final String workflowInstanceId = UUID.randomUUID().toString();
        final IniEdsInvocationETY invocation = insertIniEdsInvocation(workflowInstanceId);

        assertNotNull(invocation, "Invocation must exist to test the client call");
        mockEdsClient(false, ProcessorOperationEnum.PUBLISH);

        assertThrows(HttpServerErrorException.InternalServerError.class, () -> callPublishEdsClient(workflowInstanceId));
    }

    private void mockEdsClient(
            final boolean outcome,
            ProcessorOperationEnum operation
    ) {

        if (outcome) {
            switch (operation) {
                case PUBLISH:
                    given(edsInvocationSRV.publishByWorkflowInstanceIdAndPriority(any(PublicationRequestBodyDTO.class))).willReturn(outcome);
                case UPDATE:
                    given(edsInvocationSRV.updateByRequest(any(EdsMetadataUpdateReqDTO.class))).willReturn(Boolean.TRUE);
                case REPLACE:
                    given(edsInvocationSRV.replaceByWorkflowInstanceIdAndIdentifier(anyString(), anyString())).willReturn(Boolean.TRUE);
                case DELETE:
                    given(edsInvocationSRV.deleteByIdentifier(anyString())).willReturn(Boolean.TRUE);
            }
        } else {
            switch (operation) {
                case PUBLISH:
                    given(edsInvocationSRV.publishByWorkflowInstanceIdAndPriority(any(PublicationRequestBodyDTO.class))).willThrow(BusinessException.class);
                case UPDATE:
                    given(edsInvocationSRV.updateByRequest(any(EdsMetadataUpdateReqDTO.class))).willThrow(BusinessException.class);
                case REPLACE:
                    given(edsInvocationSRV.replaceByWorkflowInstanceIdAndIdentifier(anyString(), anyString())).willThrow(BusinessException.class);
                case DELETE:
                    given(edsInvocationSRV.deleteByIdentifier(anyString())).willThrow(BusinessException.class);
            }
        }
    }
    
}
