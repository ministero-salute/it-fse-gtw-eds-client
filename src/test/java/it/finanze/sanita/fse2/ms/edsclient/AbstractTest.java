/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.edsclient;

import java.util.List;

import org.bson.Document;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import it.finanze.sanita.fse2.ms.edsclient.dto.EdsResponseDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.EdsMetadataUpdateReqDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.IndexerValueDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.PublicationMetadataReqDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.PublicationRequestBodyDTO;
import it.finanze.sanita.fse2.ms.edsclient.enums.PriorityTypeEnum;
import it.finanze.sanita.fse2.ms.edsclient.repository.entity.IniEdsInvocationETY;
import it.finanze.sanita.fse2.ms.edsclient.utility.JsonUtility;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractTest {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    protected ServletWebServerApplicationContext webServerAppCtxt;

    @Autowired
    RestTemplate restTemplate;

    /**
     * Get transaction events generic method
     * @param workflowInstanceId of the transaction
     * @return entity containing all the events found
     */
    ResponseEntity<EdsResponseDTO> callPublishEdsClient(String workflowInstanceId) {
        String url = "http://localhost:" +
                webServerAppCtxt.getWebServer().getPort() +
                webServerAppCtxt.getServletContext().getContextPath() +
                "/v1/documents";
        PublicationRequestBodyDTO requestBodyDTO = new PublicationRequestBodyDTO();
        requestBodyDTO.setPriorityType(PriorityTypeEnum.HIGH);
        requestBodyDTO.setWorkflowInstanceId(workflowInstanceId);
        return restTemplate.postForEntity(url, requestBodyDTO, EdsResponseDTO.class);
    } 
    
    ResponseEntity<EdsResponseDTO> callUpdateEdsClient(final String idDoc, final String workflowInstanceId, PublicationMetadataReqDTO dto) {
        String url = "http://localhost:" +
                webServerAppCtxt.getWebServer().getPort() +
                webServerAppCtxt.getServletContext().getContextPath() +
                "/v1/documents/" + idDoc + "/metadata";

        PublicationMetadataReqDTO dtoUpdate = new PublicationMetadataReqDTO(); 
        HttpEntity<EdsMetadataUpdateReqDTO> request = new HttpEntity<EdsMetadataUpdateReqDTO>(new EdsMetadataUpdateReqDTO(workflowInstanceId, dtoUpdate));
        
        return restTemplate.exchange(url, HttpMethod.PUT, request, EdsResponseDTO.class);
    }
    
    
    ResponseEntity<EdsResponseDTO> callReplaceEdsClient(final String idDoc, final String workflowInstanceId) {
        String url = "http://localhost:" +
                webServerAppCtxt.getWebServer().getPort() +
                webServerAppCtxt.getServletContext().getContextPath() +
                "/v1/documents/" + idDoc;

        IndexerValueDTO dtoReplace = new IndexerValueDTO(); 
        dtoReplace.setWorkflowInstanceId(workflowInstanceId); 
        dtoReplace.setIdDoc(idDoc);
        HttpEntity<IndexerValueDTO> request = new HttpEntity<IndexerValueDTO>(dtoReplace); 
        
        return restTemplate.exchange(url, HttpMethod.PUT, request, EdsResponseDTO.class);
    } 
    
    ResponseEntity<EdsResponseDTO> callDeleteEdsClient(final String ooid) {
        String url = "http://localhost:" +
                webServerAppCtxt.getWebServer().getPort() +
                webServerAppCtxt.getServletContext().getContextPath() +
                "/v1/documents/" + ooid;
        
        return restTemplate.exchange(url, HttpMethod.DELETE, null, EdsResponseDTO.class);
    }

    @SuppressWarnings("unchecked")
    protected IniEdsInvocationETY insertIniEdsInvocation(final String workflowInstanceId) {
        final String dataString = "{\"resourceType\":\"DocumentReference\",\"masterIdentifier\":{\"id\":\"030702.TSTSMN63A01F205H.20220325112426.OQlvTq1J\"},\"identifier\":[{\"id\":\"Document00\"}],\"status\":\"current\",\"type\":{\"coding\":[{\"code\":\"11502-2\"}]},\"category\":[{\"coding\":[{\"code\":\"WOR\"}]}],\"subject\":{\"reference\":\"NGNVCN92S19L259C\"},\"date\":\"2022-06-10T20:24:08.444+02:00\",\"author\":[{\"reference\":\"MTCORV58E63L294G\"}],\"authenticator\":{\"reference\":\"GPSDGK80E76C765V\"},\"custodian\":{\"reference\":\"120148\"},\"securityLabel\":[{\"coding\":[{\"code\":\"N\"}]}],\"content\":[{\"attachment\":{\"contentType\":\"application/pdf\",\"language\":\"it-IT\",\"url\":\"string\",\"size\":57590,\"hash\":\"Y2NkMWEyM2I0YTczYzgzOGU0ZGZjMmExOTQ4YWFlYzgzODllYmQzMzFjYmFlYmMxYjMxNDRjNzRmY2ExN2RhNQ==\"},\"format\":{\"code\":\"2.16.840.1.113883.2.9.10.1.1\"}}],\"context\":{\"event\":[{\"coding\":[{\"code\":\"P99\"}]}],\"period\":{\"start\":\"1654-04-07T01:31:07+01:00\",\"end\":\"1654-04-07T01:31:07+01:00\"},\"facilityType\":{\"coding\":[{\"code\":\"Ospedale\"}]},\"practiceSetting\":{\"coding\":[{\"code\":\"Allergologia\"}]},\"sourcePatientInfo\":{\"reference\":\"NGNVCN92S19L259C\"},\"related\":[{\"reference\":\"[NRE]\"}]}}";
        final String metadataString = "[{\"submissionSetEntry\":{\"submissionTime\":\"20220617165100\",\"sourceId\":\"2.16.840.1.113883.2.9.2.80\",\"contentTypeCode\":\"PHR\",\"contentTypeCodeName\":\"Personal Health Record Update\",\"uniqueID\":\"string\"}},{\"documentEntry\":{\"mimeType\":\"application/pdf+text/x-cda-r2+xml\",\"entryUUID\":\"Document01\",\"creationTime\":\"20220617164426\",\"hash\":\"ccd1a23b4a73c838e4dfc2a1948aaec8389ebd331cbaebc1b3144c74fca17da5\",\"size\":57590,\"status\":\"approved\",\"languageCode\":\"it-IT\",\"patientId\":\"120148\",\"confidentialityCode\":\"N\",\"confidentialityCodeDisplayName\":\"Normal\",\"typeCode\":\"11502-2\",\"typeCodeName\":\"Referto di laboratorio\",\"formatCode\":\"2.16.840.1.113883.2.9.10.1.1\",\"formatCodeName\":\"Referto di Laboratorio\",\"legalAuthenticator\":\"GPSDGK80E76C765V\",\"sourcePatientInfo\":\"100 120 RM Roma 058091 00187 Via Aurora 12 100 120 RM Roma 058091 00138 Via Canevari 12B Verdi Giuseppe 100 120 RM Roma 058091\",\"author\":\"\",\"representedOrganizationName\":\"Nuovo Ospedale S.Agostino (MO)\",\"representedOrganizationCode\":\"080105\",\"uniqueId\":\"2.16.840.1.113883.2.9.2.120.4.4^030702.TSTSMN63A01F205H.20220325112426.OQlvTq1J\",\"referenceIdList\":[\"[NRE]\"],\"healthcareFacilityTypeCode\":\"Ospedale\",\"healthcareFacilityTypeCodeName\":\"Ospedale\",\"eventCodeList\":[\"P99\"],\"repositoryUniqueId\":\"2.16.840.1.113883.2.9.2.80.4.5.999\",\"classCode\":\"WOR\",\"classCodeName\":\"Documento di workflow\",\"practiceSettingCode\":\"AD_PSC001\",\"practiceSettingCodeName\":\"Allergologia\",\"sourcePatientId\":\"NGNVCN92S19L259C\",\"serviceStartTime\":\"20220617164426\",\"serviceStopTime\":\"20220617164426\"}},{\"tokenEntry\":{\"header\":{\"alg\":\"RS256\",\"typ\":\"JWT\",\"x5c\":\"X5C cert base 64\"},\"payload\":{\"iss\":\"201123456\",\"iat\":1540890704,\"exp\":1540918800,\"jti\":\"1540918800\",\"aud\":\"fse-gateway\",\"sub\":\"RSSMRA22A01A399Z\",\"subject_organization_id\":\"110\",\"subject_organization\":\"Regione Marche\",\"locality\":\"201123456\",\"subject_role\":\"AAS\",\"person_id\":\"BMTBTS01A01I526W\",\"patient_consent\":true,\"purpose_of_use\":\"TREATMENT\",\"action_id\":\"CREATE\",\"attachment_hash\":\"ccd1a23b4a73c838e4dfc2a1948aaec8389ebd331cbaebc1b3144c74fca17da5\"}}}]";
        final Document data = JsonUtility.jsonToObject(dataString, Document.class);
        final List<Document> metadata = JsonUtility.jsonToObject(metadataString, List.class);

        IniEdsInvocationETY iniEdsInvocationETY = new IniEdsInvocationETY();
        iniEdsInvocationETY.setWorkflowInstanceId(workflowInstanceId);
        iniEdsInvocationETY.setData(data);
        iniEdsInvocationETY.setMetadata(metadata);

        mongoTemplate.save(iniEdsInvocationETY);

        return iniEdsInvocationETY;
    }
}
