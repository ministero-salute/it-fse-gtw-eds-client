/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package it.finanze.sanita.fse2.ms.edsclient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.util.UUID;

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
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import it.finanze.sanita.fse2.ms.edsclient.config.Constants;
import it.finanze.sanita.fse2.ms.edsclient.dto.EdsResponseDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.PublicationMetadataReqDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.response.DocumentResponseDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.edsclient.enums.ProcessorOperationEnum;
import it.finanze.sanita.fse2.ms.edsclient.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.edsclient.repository.entity.IniEdsInvocationETY;
import it.finanze.sanita.fse2.ms.edsclient.utility.JsonUtility;

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

        final ResponseEntity<EdsResponseDTO> response = callPublishEdsClient(TEST_WORKFLOW_INSTANCE_ID);

        assertNotNull(response, "A response should be returned");

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Calling the client should return 200 OK");
        assertTrue(response.getBody().isEsito(), "The outcome should be true");
    }

    @Test
    void publishConnectionRefusedTest() {
        mockEdsClient(ProcessorOperationEnum.PUBLISH, HttpStatus.REQUEST_TIMEOUT);

        ResponseEntity<EdsResponseDTO> response = callPublishEdsClient(TEST_WORKFLOW_INSTANCE_ID);
        assertNotNull(response);
        assertFalse(response.getBody().isEsito());
        assertNotNull(response.getBody().getMessageError());
        assertNotNull(invocation, "Invocation must exist to test the client call");
    }

    @Test
    void publishGenericExceptionTest() {
        final IniEdsInvocationETY invocation = insertIniEdsInvocation(TEST_WORKFLOW_INSTANCE_ID);

        mockEdsClient(ProcessorOperationEnum.PUBLISH, HttpStatus.BAD_GATEWAY);
        assertNotNull(invocation, "Invocation must exist to test the client call");
        
        ResponseEntity<EdsResponseDTO> response = callPublishEdsClient(TEST_WORKFLOW_INSTANCE_ID);
        assertNotNull(response);
        assertFalse(response.getBody().isEsito());
        assertNotNull(response.getBody().getMessageError());
    }

    @Test
    void updateSuccessTest() {

        final String docId = UUID.randomUUID().toString();

        String req = "{\"tipologiaStruttura\":\"Ospedale\",\"attiCliniciRegoleAccesso\":[\"P99\"],\"tipoDocumentoLivAlto\":\"WOR\",\"assettoOrganizzativo\":\"AD_PSC001\",\"dataInizioPrestazione\":\"1661246473\",\"dataFinePrestazione\":\"1661246473\",\"conservazioneANorma\":\"string\",\"tipoAttivitaClinica\":\"PHR\",\"identificativoSottomissione\":\"2.16.840.1.113883.2.9.2.90.4.4^090A02205783394_PRESPEC\"}";
        PublicationMetadataReqDTO publicationMetadataReqDTO = JsonUtility.jsonToObject(req, PublicationMetadataReqDTO.class);
        mockEdsClient(ProcessorOperationEnum.UPDATE, HttpStatus.OK);

        final ResponseEntity<EdsResponseDTO> response = callUpdateEdsClient(docId, TEST_WORKFLOW_INSTANCE_ID, publicationMetadataReqDTO);

        assertNotNull(response, "A response should be returned");

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Calling the client should return 200 OK");
        assertTrue(response.getBody().isEsito(), "The outcome should be true");
    }

    @Test
    void updateConnectionRefusedTest() {

        final String docId = UUID.randomUUID().toString();

        String req = "{\"tipologiaStruttura\":\"Ospedale\",\"attiCliniciRegoleAccesso\":[\"P99\"],\"tipoDocumentoLivAlto\":\"WOR\",\"assettoOrganizzativo\":\"AD_PSC001\",\"dataInizioPrestazione\":\"1661246473\",\"dataFinePrestazione\":\"1661246473\",\"conservazioneANorma\":\"string\",\"tipoAttivitaClinica\":\"PHR\",\"identificativoSottomissione\":\"2.16.840.1.113883.2.9.2.90.4.4^090A02205783394_PRESPEC\"}";
        PublicationMetadataReqDTO publicationMetadataReqDTO = JsonUtility.jsonToObject(req, PublicationMetadataReqDTO.class);

        mockEdsClient(ProcessorOperationEnum.UPDATE, HttpStatus.REQUEST_TIMEOUT);
        ResponseEntity<EdsResponseDTO> response = callUpdateEdsClient(docId, TEST_WORKFLOW_INSTANCE_ID, publicationMetadataReqDTO);
        assertNotNull(response);
        assertFalse(response.getBody().isEsito());
        assertNotNull(response.getBody().getMessageError());
    }

    @Test
    void updateGenericExceptionTest() {
        final String docId = UUID.randomUUID().toString();

        String req = "{\"tipologiaStruttura\":\"Ospedale\",\"attiCliniciRegoleAccesso\":[\"P99\"],\"tipoDocumentoLivAlto\":\"WOR\",\"assettoOrganizzativo\":\"AD_PSC001\",\"dataInizioPrestazione\":\"1661246473\",\"dataFinePrestazione\":\"1661246473\",\"conservazioneANorma\":\"string\",\"tipoAttivitaClinica\":\"PHR\",\"identificativoSottomissione\":\"2.16.840.1.113883.2.9.2.90.4.4^090A02205783394_PRESPEC\"}";
        PublicationMetadataReqDTO publicationMetadataReqDTO = JsonUtility.jsonToObject(req, PublicationMetadataReqDTO.class);

        mockEdsClient(ProcessorOperationEnum.UPDATE, HttpStatus.BAD_GATEWAY);

        ResponseEntity<EdsResponseDTO> response = callUpdateEdsClient(docId, TEST_WORKFLOW_INSTANCE_ID, publicationMetadataReqDTO);
        assertNotNull(response);
        assertFalse(response.getBody().isEsito());
        assertNotNull(response.getBody().getMessageError());
    }

    @Test
    void replaceSuccessTest() {
        final String docId = UUID.randomUUID().toString();

        String req = "{\"tipologiaStruttura\":\"Ospedale\",\"attiCliniciRegoleAccesso\":[\"P99\"],\"tipoDocumentoLivAlto\":\"WOR\",\"assettoOrganizzativo\":\"AD_PSC001\",\"dataInizioPrestazione\":\"1661246473\",\"dataFinePrestazione\":\"1661246473\",\"conservazioneANorma\":\"string\",\"tipoAttivitaClinica\":\"PHR\",\"identificativoSottomissione\":\"2.16.840.1.113883.2.9.2.90.4.4^090A02205783394_PRESPEC\"}";
        JsonUtility.jsonToObject(req, PublicationMetadataReqDTO.class);
        mockEdsClient(ProcessorOperationEnum.REPLACE, HttpStatus.OK);

        final ResponseEntity<EdsResponseDTO> response = callReplaceEdsClient(docId, TEST_WORKFLOW_INSTANCE_ID);

        assertNotNull(response, "A response should be returned");

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Calling the client should return 200 OK");
        assertTrue(response.getBody().isEsito(), "The outcome should be true");
    }

    @Test
    void replaceConnectionRefusedTest() {

        final String docId = UUID.randomUUID().toString();

        String req = "{\"tipologiaStruttura\":\"Ospedale\",\"attiCliniciRegoleAccesso\":[\"P99\"],\"tipoDocumentoLivAlto\":\"WOR\",\"assettoOrganizzativo\":\"AD_PSC001\",\"dataInizioPrestazione\":\"1661246473\",\"dataFinePrestazione\":\"1661246473\",\"conservazioneANorma\":\"string\",\"tipoAttivitaClinica\":\"PHR\",\"identificativoSottomissione\":\"2.16.840.1.113883.2.9.2.90.4.4^090A02205783394_PRESPEC\"}";
        JsonUtility.jsonToObject(req, PublicationMetadataReqDTO.class);
        mockEdsClient(ProcessorOperationEnum.REPLACE, HttpStatus.BAD_REQUEST);
        ResponseEntity<EdsResponseDTO> response = callReplaceEdsClient(docId, TEST_WORKFLOW_INSTANCE_ID);
        assertNotNull(response);
        assertFalse(response.getBody().isEsito());
        assertNotNull(response.getBody().getMessageError());
    }

    @Test
    void replaceGenericExceptionTest() {
        final String docId = UUID.randomUUID().toString();

        String req = "{\"tipologiaStruttura\":\"Ospedale\",\"attiCliniciRegoleAccesso\":[\"P99\"],\"tipoDocumentoLivAlto\":\"WOR\",\"assettoOrganizzativo\":\"AD_PSC001\",\"dataInizioPrestazione\":\"1661246473\",\"dataFinePrestazione\":\"1661246473\",\"conservazioneANorma\":\"string\",\"tipoAttivitaClinica\":\"PHR\",\"identificativoSottomissione\":\"2.16.840.1.113883.2.9.2.90.4.4^090A02205783394_PRESPEC\"}";
        JsonUtility.jsonToObject(req, PublicationMetadataReqDTO.class);
        mockEdsClient(ProcessorOperationEnum.REPLACE, HttpStatus.BAD_GATEWAY);
        ResponseEntity<EdsResponseDTO> response = callReplaceEdsClient(docId, TEST_WORKFLOW_INSTANCE_ID);
        assertNotNull(response);
        assertFalse(response.getBody().isEsito());
        assertNotNull(response.getBody().getMessageError());
    }

    @Test
    void deleteTest() {

    	final String docId = UUID.randomUUID().toString();
        mockEdsClient(ProcessorOperationEnum.DELETE, HttpStatus.OK);
        
        final ResponseEntity<EdsResponseDTO> response = callDeleteEdsClient(docId); 

        assertNotNull(response, "A response should be returned");

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Calling the client should return 200 OK");
        assertTrue(response.getBody().isEsito(), "The outcome should be true");
    }

    @Test
    void deleteConnectionRefusedTest() {

        final String docId = UUID.randomUUID().toString();
        mockEdsClient(ProcessorOperationEnum.DELETE, HttpStatus.BAD_REQUEST);
        ResponseEntity<EdsResponseDTO> response = callDeleteEdsClient(docId);
        assertNotNull(response);
        assertFalse(response.getBody().isEsito());
        assertNotNull(response.getBody().getMessageError());
    }

    @Test
    void deleteGenericExceptionTest() {
        final String docId = UUID.randomUUID().toString();
        mockEdsClient(ProcessorOperationEnum.DELETE, HttpStatus.BAD_GATEWAY);
        ResponseEntity<EdsResponseDTO> response = callDeleteEdsClient(docId);
        assertNotNull(response);
        assertFalse(response.getBody().isEsito());
        assertNotNull(response.getBody().getMessageError());
    }

    private void mockEdsClient(ProcessorOperationEnum operation, HttpStatus status) {
        LogTraceInfoDTO logTraceInfoDTO = new LogTraceInfoDTO("spanId", "traceId");
        DocumentResponseDTO mockResponse = new DocumentResponseDTO(logTraceInfoDTO);
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
