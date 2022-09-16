package it.finanze.sanita.fse2.ms.edsclient.client.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import it.finanze.sanita.fse2.ms.edsclient.client.IEdsClient;
import it.finanze.sanita.fse2.ms.edsclient.config.EdsCFG;
import it.finanze.sanita.fse2.ms.edsclient.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.response.DocumentResponseDTO;
import it.finanze.sanita.fse2.ms.edsclient.enums.ProcessorOperationEnum;
import it.finanze.sanita.fse2.ms.edsclient.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.edsclient.exceptions.ConnectionRefusedException;
import it.finanze.sanita.fse2.ms.edsclient.repository.entity.IniEdsInvocationETY;
import it.finanze.sanita.fse2.ms.edsclient.utility.JsonUtility;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(value="mock.disabled")
public class EdsClient implements IEdsClient {

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 5665880440554069040L;

    @Autowired
    private transient RestTemplate restTemplate;

    @Autowired
    private EdsCFG edsCFG;

    @Override
    public Boolean sendData(final IniEdsInvocationETY iniEdsInvocationETY) {
        log.info("Calling eds ingestion ep - START");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        DocumentReferenceDTO requestBody = new DocumentReferenceDTO();
        requestBody.setIdentifier(iniEdsInvocationETY.getWorkflowInstanceId());
        requestBody.setOperation(ProcessorOperationEnum.CREATE);
        requestBody.setJsonString(JsonUtility.objectToJson(iniEdsInvocationETY));

        HttpEntity<?> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<DocumentResponseDTO> response = null;
        String url = edsCFG.getEdsIngestionHost() + "/v1/document";
        try {
            response = restTemplate.exchange(url,
                    HttpMethod.POST, entity,
                    DocumentResponseDTO.class);
            log.info("{} status returned from eds", response.getStatusCode());
        } catch(ResourceAccessException cex) {
            log.error("Connect error while call eds ingestion ep :" + cex);
            throw new ConnectionRefusedException(edsCFG.getEdsIngestionHost(),"Connection refused");
        } catch(Exception ex) {
            log.error("Generic error while call eds ingestion ep :" + ex);
            throw new BusinessException("Generic error while call eds ingestion ep :" + ex);
        }

        return response.getStatusCode().is2xxSuccessful();
    }
}
