package it.finanze.sanita.fse2.ms.edsclient;

import it.finanze.sanita.fse2.ms.edsclient.config.Constants;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.PublicationMetadataReqDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.response.DocumentResponseDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.response.EDSPublicationResponseDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.edsclient.enums.ProcessorOperationEnum;
import it.finanze.sanita.fse2.ms.edsclient.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.edsclient.repository.entity.IniEdsInvocationETY;
import it.finanze.sanita.fse2.ms.edsclient.utility.JsonUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = {Constants.ComponentScan.BASE})
@ActiveProfiles(Constants.Profile.TEST)
class EdsClientTest extends AbstractTest {

    @Autowired
    MongoTemplate mongoTemplate;

    @SpyBean
    private RestTemplate restTemplate;

    private static final String TEST_WORKFLOW_INSTANCE_ID = UUID.randomUUID().toString();
    IniEdsInvocationETY invocation;

    @BeforeEach
    void setup() {
        mongoTemplate.dropCollection(IniEdsInvocationETY.class);
        invocation = insertIniEdsInvocation(TEST_WORKFLOW_INSTANCE_ID);
    }

    @Test
    void publishSuccessTest() {
        assertNotNull(invocation, "Invocation must exist to test the client call");
        mockEdsClient(ProcessorOperationEnum.PUBLISH, HttpStatus.OK);

        final ResponseEntity<EDSPublicationResponseDTO> response = callPublishEdsClient(TEST_WORKFLOW_INSTANCE_ID);

        assertNotNull(response, "A response should be returned");

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Calling the client should return 200 OK");
        assertTrue(response.getBody().getEsito(), "The outcome should be true");
        assertNotNull(response.getBody().getSpanID(), "The span id should be returned");
        assertNotNull(response.getBody().getTraceID(), "The trace id should be returned");
    }

    @Test
    void publishConnectionRefusedTest() {
        mockEdsClient(ProcessorOperationEnum.PUBLISH, HttpStatus.REQUEST_TIMEOUT);

        assertNotNull(invocation, "Invocation must exist to test the client call");
        assertThrows(HttpServerErrorException.InternalServerError.class, () -> callPublishEdsClient(TEST_WORKFLOW_INSTANCE_ID));
    }

    @Test
    void publishGenericExceptionTest() {
        final IniEdsInvocationETY invocation = insertIniEdsInvocation(TEST_WORKFLOW_INSTANCE_ID);

        mockEdsClient(ProcessorOperationEnum.PUBLISH, HttpStatus.BAD_GATEWAY);

        assertNotNull(invocation, "Invocation must exist to test the client call");
        assertThrows(HttpServerErrorException.InternalServerError.class, () -> callPublishEdsClient(TEST_WORKFLOW_INSTANCE_ID));
    }

    @Test
    void updateSuccessTest() {

        final String docId = UUID.randomUUID().toString();

        String req = "{\"tipologiaStruttura\":\"Ospedale\",\"attiCliniciRegoleAccesso\":[\"P99\"],\"tipoDocumentoLivAlto\":\"WOR\",\"assettoOrganizzativo\":\"AD_PSC001\",\"dataInizioPrestazione\":\"1661246473\",\"dataFinePrestazione\":\"1661246473\",\"conservazioneANorma\":\"string\",\"tipoAttivitaClinica\":\"PHR\",\"identificativoSottomissione\":\"2.16.840.1.113883.2.9.2.90.4.4^090A02205783394_PRESPEC\"}";
        PublicationMetadataReqDTO publicationMetadataReqDTO = JsonUtility.jsonToObject(req, PublicationMetadataReqDTO.class);
        mockEdsClient(ProcessorOperationEnum.UPDATE, HttpStatus.OK);

        final ResponseEntity<EDSPublicationResponseDTO> response = callUpdateEdsClient(docId, TEST_WORKFLOW_INSTANCE_ID, publicationMetadataReqDTO);

        assertNotNull(response, "A response should be returned");

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Calling the client should return 200 OK");
        assertTrue(response.getBody().getEsito(), "The outcome should be true");
        assertNotNull(response.getBody().getSpanID(), "The span id should be returned");
        assertNotNull(response.getBody().getTraceID(), "The trace id should be returned");
    }

    @Test
    void updateConnectionRefusedTest() {

        final String docId = UUID.randomUUID().toString();

        String req = "{\"tipologiaStruttura\":\"Ospedale\",\"attiCliniciRegoleAccesso\":[\"P99\"],\"tipoDocumentoLivAlto\":\"WOR\",\"assettoOrganizzativo\":\"AD_PSC001\",\"dataInizioPrestazione\":\"1661246473\",\"dataFinePrestazione\":\"1661246473\",\"conservazioneANorma\":\"string\",\"tipoAttivitaClinica\":\"PHR\",\"identificativoSottomissione\":\"2.16.840.1.113883.2.9.2.90.4.4^090A02205783394_PRESPEC\"}";
        PublicationMetadataReqDTO publicationMetadataReqDTO = JsonUtility.jsonToObject(req, PublicationMetadataReqDTO.class);

        mockEdsClient(ProcessorOperationEnum.UPDATE, HttpStatus.REQUEST_TIMEOUT);

        assertThrows(HttpServerErrorException.InternalServerError.class, () -> callUpdateEdsClient(docId, TEST_WORKFLOW_INSTANCE_ID, publicationMetadataReqDTO));
    }

    @Test
    void updateGenericExceptionTest() {
        final String docId = UUID.randomUUID().toString();

        String req = "{\"tipologiaStruttura\":\"Ospedale\",\"attiCliniciRegoleAccesso\":[\"P99\"],\"tipoDocumentoLivAlto\":\"WOR\",\"assettoOrganizzativo\":\"AD_PSC001\",\"dataInizioPrestazione\":\"1661246473\",\"dataFinePrestazione\":\"1661246473\",\"conservazioneANorma\":\"string\",\"tipoAttivitaClinica\":\"PHR\",\"identificativoSottomissione\":\"2.16.840.1.113883.2.9.2.90.4.4^090A02205783394_PRESPEC\"}";
        PublicationMetadataReqDTO publicationMetadataReqDTO = JsonUtility.jsonToObject(req, PublicationMetadataReqDTO.class);

        mockEdsClient(ProcessorOperationEnum.UPDATE, HttpStatus.BAD_GATEWAY);

        assertThrows(HttpServerErrorException.InternalServerError.class, () -> callUpdateEdsClient(docId, TEST_WORKFLOW_INSTANCE_ID, publicationMetadataReqDTO));
    }

    @Test
    void replaceSuccessTest() {
        final String docId = UUID.randomUUID().toString();

        String req = "{\"tipologiaStruttura\":\"Ospedale\",\"attiCliniciRegoleAccesso\":[\"P99\"],\"tipoDocumentoLivAlto\":\"WOR\",\"assettoOrganizzativo\":\"AD_PSC001\",\"dataInizioPrestazione\":\"1661246473\",\"dataFinePrestazione\":\"1661246473\",\"conservazioneANorma\":\"string\",\"tipoAttivitaClinica\":\"PHR\",\"identificativoSottomissione\":\"2.16.840.1.113883.2.9.2.90.4.4^090A02205783394_PRESPEC\"}";
        JsonUtility.jsonToObject(req, PublicationMetadataReqDTO.class);
        mockEdsClient(ProcessorOperationEnum.REPLACE, HttpStatus.OK);

        final ResponseEntity<EDSPublicationResponseDTO> response = callReplaceEdsClient(docId, TEST_WORKFLOW_INSTANCE_ID);

        assertNotNull(response, "A response should be returned");

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Calling the client should return 200 OK");
        assertTrue(response.getBody().getEsito(), "The outcome should be true");
        assertNotNull(response.getBody().getSpanID(), "The span id should be returned");
        assertNotNull(response.getBody().getTraceID(), "The trace id should be returned");
    }

    @Test
    void replaceConnectionRefusedTest() {

        final String docId = UUID.randomUUID().toString();

        String req = "{\"tipologiaStruttura\":\"Ospedale\",\"attiCliniciRegoleAccesso\":[\"P99\"],\"tipoDocumentoLivAlto\":\"WOR\",\"assettoOrganizzativo\":\"AD_PSC001\",\"dataInizioPrestazione\":\"1661246473\",\"dataFinePrestazione\":\"1661246473\",\"conservazioneANorma\":\"string\",\"tipoAttivitaClinica\":\"PHR\",\"identificativoSottomissione\":\"2.16.840.1.113883.2.9.2.90.4.4^090A02205783394_PRESPEC\"}";
        JsonUtility.jsonToObject(req, PublicationMetadataReqDTO.class);
        mockEdsClient(ProcessorOperationEnum.REPLACE, HttpStatus.BAD_REQUEST);

        assertThrows(HttpServerErrorException.InternalServerError.class, () -> callReplaceEdsClient(docId, TEST_WORKFLOW_INSTANCE_ID));
    }

    @Test
    void replaceGenericExceptionTest() {
        final String docId = UUID.randomUUID().toString();

        String req = "{\"tipologiaStruttura\":\"Ospedale\",\"attiCliniciRegoleAccesso\":[\"P99\"],\"tipoDocumentoLivAlto\":\"WOR\",\"assettoOrganizzativo\":\"AD_PSC001\",\"dataInizioPrestazione\":\"1661246473\",\"dataFinePrestazione\":\"1661246473\",\"conservazioneANorma\":\"string\",\"tipoAttivitaClinica\":\"PHR\",\"identificativoSottomissione\":\"2.16.840.1.113883.2.9.2.90.4.4^090A02205783394_PRESPEC\"}";
        JsonUtility.jsonToObject(req, PublicationMetadataReqDTO.class);
        mockEdsClient(ProcessorOperationEnum.REPLACE, HttpStatus.BAD_GATEWAY);

        assertThrows(HttpServerErrorException.InternalServerError.class, () -> callReplaceEdsClient(docId, TEST_WORKFLOW_INSTANCE_ID));
    }

    @Test
    void deleteTest() {

    	final String docId = UUID.randomUUID().toString();
        mockEdsClient(ProcessorOperationEnum.DELETE, HttpStatus.OK);
        
        final ResponseEntity<EDSPublicationResponseDTO> response = callDeleteEdsClient(docId); 

        assertNotNull(response, "A response should be returned");

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Calling the client should return 200 OK");
        assertTrue(response.getBody().getEsito(), "The outcome should be true");
        assertNotNull(response.getBody().getSpanID(), "The span id should be returned");
        assertNotNull(response.getBody().getTraceID(), "The trace id should be returned");
    }

    @Test
    void deleteConnectionRefusedTest() {

        final String docId = UUID.randomUUID().toString();
        mockEdsClient(ProcessorOperationEnum.DELETE, HttpStatus.BAD_REQUEST);

        assertThrows(HttpServerErrorException.InternalServerError.class, () -> callDeleteEdsClient(docId));
    }

    @Test
    void deleteGenericExceptionTest() {
        final String docId = UUID.randomUUID().toString();
        mockEdsClient(ProcessorOperationEnum.DELETE, HttpStatus.BAD_GATEWAY);

        assertThrows(HttpServerErrorException.InternalServerError.class, () -> callDeleteEdsClient(docId));
    }

    private void mockEdsClient(ProcessorOperationEnum operation, HttpStatus status) {
        LogTraceInfoDTO logTraceInfoDTO = new LogTraceInfoDTO("spanId", "traceId");
        DocumentResponseDTO mockResponse = new DocumentResponseDTO(logTraceInfoDTO, "transactionId");
        if (status.equals(HttpStatus.OK)) {
            doReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK))
                    .when(restTemplate).exchange(anyString(), eq(Constants.AppConstants.methodMap.get(operation)), any(HttpEntity.class), eq(DocumentResponseDTO.class));
        } else if (status.is4xxClientError()) {
            doThrow(new ResourceAccessException(""))
                    .when(restTemplate).exchange(anyString(), eq(Constants.AppConstants.methodMap.get(operation)), any(HttpEntity.class), eq(DocumentResponseDTO.class));
        } else {
            doThrow(new BusinessException(""))
                    .when(restTemplate).exchange(anyString(), eq(Constants.AppConstants.methodMap.get(operation)), any(HttpEntity.class), eq(DocumentResponseDTO.class));
        }
    }
}
