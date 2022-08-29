package it.finanze.sanita.fse2.ms.edsclient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import it.finanze.sanita.fse2.ms.edsclient.dto.response.EDSPublicationResponseDTO;
import it.finanze.sanita.fse2.ms.edsclient.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.edsclient.repository.entity.IniEdsInvocationETY;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "mock.disabled=false")
@ComponentScan(basePackages = {Constants.ComponentScan.BASE})
@ActiveProfiles(Constants.Profile.TEST)
class EdsClientTest extends AbstractTest {

    @Autowired
    MongoTemplate mongoTemplate;

    @MockBean
    IEdsClient edsClient;

    @BeforeEach
    void setup() {
        mongoTemplate.dropCollection(IniEdsInvocationETY.class);
    }

    @Test
    void publishTest() {

        final String workflowInstanceId = UUID.randomUUID().toString();
        final IniEdsInvocationETY invocation = insertIniEdsInvocation(workflowInstanceId);

        assertNotNull(invocation, "Invocation must exist to test the client call");
        mockEdsClient(invocation, true);

        final ResponseEntity<EDSPublicationResponseDTO> response = callEdsClient(workflowInstanceId);

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
        mockEdsClient(invocation, false);

        assertThrows(HttpServerErrorException.InternalServerError.class, () -> callEdsClient(workflowInstanceId));
    }

    private void mockEdsClient(final IniEdsInvocationETY invocation, final boolean outcome) {

        if (outcome) {
            given(edsClient.sendData(invocation)).willReturn(outcome);
        } else {
            given(edsClient.sendData(invocation)).willThrow(BusinessException.class);
        }
    }
    
}
