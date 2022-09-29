package it.finanze.sanita.fse2.ms.edsclient.client.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import it.finanze.sanita.fse2.ms.edsclient.client.IEdsClient;
import it.finanze.sanita.fse2.ms.edsclient.config.Constants;
import it.finanze.sanita.fse2.ms.edsclient.config.EdsCFG;
import it.finanze.sanita.fse2.ms.edsclient.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.IngestorRequestDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.response.DocumentResponseDTO;
import it.finanze.sanita.fse2.ms.edsclient.enums.ProcessorOperationEnum;
import it.finanze.sanita.fse2.ms.edsclient.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.edsclient.exceptions.ConnectionRefusedException;
import it.finanze.sanita.fse2.ms.edsclient.utility.JsonUtility;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EdsClient implements IEdsClient {

    private static final String MSG_UNSUPPORTED = "Unsupported exception";

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 5665880440554069040L;

    @Autowired
    private transient RestTemplate restTemplate;

    @Autowired
    private EdsCFG edsCFG;

    @Override
    public Boolean dispatchAndSendData(IngestorRequestDTO ingestorRequestDTO) {
        log.info("Calling eds ingestion ep - START"); 
        log.info("Operation: " + ingestorRequestDTO.getOperation().getName());
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json"); 
        
        DocumentReferenceDTO requestBody = buildRequestBody(ingestorRequestDTO);
        
        HttpEntity<?> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<DocumentResponseDTO> response = null;
        String url = edsCFG.getEdsIngestionHost() + "/v1/document";
        try {
            response = restTemplate.exchange(url,Constants.AppConstants.methodMap.get(ingestorRequestDTO.getOperation()), entity,
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
    
    
    private DocumentReferenceDTO buildRequestBody(IngestorRequestDTO ingestorRequestDTO) {
        DocumentReferenceDTO requestBody = null; 
        
        
        switch(ingestorRequestDTO.getOperation()) {
            case UPDATE:
                if (ingestorRequestDTO.getUpdateReqDTO() == null) {
                    // bad request
                    throw new BusinessException(MSG_UNSUPPORTED);
                }
                requestBody = new DocumentReferenceDTO();
                requestBody.setIdentifier(ingestorRequestDTO.getIdentifier());
                requestBody.setOperation(ProcessorOperationEnum.UPDATE);
                requestBody.setJsonString(JsonUtility.objectToJson(ingestorRequestDTO.getUpdateReqDTO()));
                break;
			case REPLACE:
                if (ingestorRequestDTO.getIniEdsInvocationETY() == null) {
                    // bad request
                    throw new BusinessException(MSG_UNSUPPORTED);
                }
	        	requestBody = new DocumentReferenceDTO(); 
	            requestBody.setIdentifier(ingestorRequestDTO.getIdentifier());
	            requestBody.setOperation(ProcessorOperationEnum.REPLACE);
	            requestBody.setJsonString(JsonUtility.objectToJson(ingestorRequestDTO.getIniEdsInvocationETY().getData()));
	        	break; 
	        	
	        case DELETE: 
	        	break;

            case PUBLISH:
	        default:
                if (ingestorRequestDTO.getIniEdsInvocationETY() == null) {
                    // bad request
                    throw new BusinessException(MSG_UNSUPPORTED);
                }
	        	requestBody = new DocumentReferenceDTO();
	            requestBody.setIdentifier(ingestorRequestDTO.getIdentifier());
	            requestBody.setOperation(ProcessorOperationEnum.PUBLISH);
	            requestBody.setJsonString(JsonUtility.objectToJson(ingestorRequestDTO.getIniEdsInvocationETY().getData()));
                requestBody.setPriorityType(ingestorRequestDTO.getPriorityType());
	        	break; 
        } 
        
        return requestBody; 

    }
}
