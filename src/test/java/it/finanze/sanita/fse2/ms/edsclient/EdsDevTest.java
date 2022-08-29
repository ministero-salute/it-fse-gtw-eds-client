package it.finanze.sanita.fse2.ms.edsclient;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpServerErrorException;

import it.finanze.sanita.fse2.ms.edsclient.config.Constants;
import it.finanze.sanita.fse2.ms.edsclient.repository.entity.IniEdsInvocationETY;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "mock.disabled=true")
@ComponentScan(basePackages = {Constants.ComponentScan.BASE})
@ActiveProfiles(Constants.Profile.DEV)
class EdsDevTest extends AbstractTest {

    @Autowired
    MongoTemplate mongoTemplate;

    @Test
    void publishTestOnLocalFhir() {

        final String workflowInstanceId = UUID.randomUUID().toString();
        final IniEdsInvocationETY invocation = insertIniEdsInvocation(workflowInstanceId);

        assertNotNull(invocation, "Invocation must exist to test the client call");
        assertThrows(HttpServerErrorException.InternalServerError.class, () -> callEdsClient(workflowInstanceId));
    }
    
}
