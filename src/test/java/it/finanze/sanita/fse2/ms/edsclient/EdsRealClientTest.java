package it.finanze.sanita.fse2.ms.edsclient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.util.UUID;

import it.finanze.sanita.fse2.ms.edsclient.dto.response.DocumentResponseDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.response.EDSPublicationResponseDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.edsclient.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.edsclient.exceptions.ConnectionRefusedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpServerErrorException;

import it.finanze.sanita.fse2.ms.edsclient.config.Constants;
import it.finanze.sanita.fse2.ms.edsclient.repository.entity.IniEdsInvocationETY;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "mock.disabled=true")
@ComponentScan(basePackages = {Constants.ComponentScan.BASE})
@ActiveProfiles(Constants.Profile.TEST)
class EdsRealClientTest extends AbstractTest {

    @Autowired
    MongoTemplate mongoTemplate;

    @SpyBean
    RestTemplate restTemplate;

    @Test
    void publishErrorTestWithRealClient() {

        final String workflowInstanceId = UUID.randomUUID().toString();
        final IniEdsInvocationETY invocation = insertIniEdsInvocation(workflowInstanceId);
        mockEdsClient(invocation, false, HttpStatus.INTERNAL_SERVER_ERROR);
        assertNotNull(invocation, "Invocation must exist to test the client call");
        assertThrows(HttpServerErrorException.InternalServerError.class, () -> callEdsClient(workflowInstanceId));
    }

    @Test
    void publishConnectionRefusedTestWithRealClient() {

        final String workflowInstanceId = UUID.randomUUID().toString();
        final IniEdsInvocationETY invocation = insertIniEdsInvocation(workflowInstanceId);

        mockEdsClient(invocation, false, HttpStatus.REQUEST_TIMEOUT);
        assertNotNull(invocation, "Invocation must exist to test the client call");
        assertThrows(HttpServerErrorException.InternalServerError.class, () -> callEdsClient(workflowInstanceId));
    }

    @Test
    void publishGenericExceptionTestWithRealClient() {
        final String workflowInstanceId = UUID.randomUUID().toString();
        final IniEdsInvocationETY invocation = insertIniEdsInvocation(workflowInstanceId);

        mockEdsClient(invocation, false, HttpStatus.OK);
        assertNotNull(invocation, "Invocation must exist to test the client call");
        ResponseEntity<EDSPublicationResponseDTO> response = callEdsClient(workflowInstanceId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        EDSPublicationResponseDTO mockResponse = new EDSPublicationResponseDTO();
        mockResponse.setEsito(false);
        assertEquals(mockResponse.getEsito(), response.getBody().getEsito());
    }


    @Test
    void publishSuccessTestWithRealClient() {

        final String workflowInstanceId = UUID.randomUUID().toString();
        final IniEdsInvocationETY invocation = insertIniEdsInvocation(workflowInstanceId);

        mockEdsClient(invocation, true, HttpStatus.OK);
        assertNotNull(invocation, "Invocation must exist to test the client call");
        ResponseEntity<EDSPublicationResponseDTO> response = callEdsClient(workflowInstanceId);
        EDSPublicationResponseDTO mockResponse = new EDSPublicationResponseDTO();
        mockResponse.setEsito(true);
        assertEquals(mockResponse.getEsito(), response.getBody().getEsito());
    }

    private void mockEdsClient(final IniEdsInvocationETY invocation, final boolean outcome, HttpStatus status) {
        LogTraceInfoDTO logTraceInfoDTO = new LogTraceInfoDTO("spanId", "traceId");
        DocumentResponseDTO mockResponse = new DocumentResponseDTO(logTraceInfoDTO, "transactionId");
        if (outcome) {
            doReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK)).when(restTemplate).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(DocumentResponseDTO.class));
        } else if (status.is5xxServerError()) {
            doThrow(HttpServerErrorException.InternalServerError.class).when(restTemplate).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(DocumentResponseDTO.class));
        } else if (status.is4xxClientError()) {
            doThrow(new ResourceAccessException("")).when(restTemplate).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(DocumentResponseDTO.class));
        } else {
            doReturn(new ResponseEntity<>(null, HttpStatus.BAD_REQUEST)).when(restTemplate).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(DocumentResponseDTO.class));
        }
    }
}
