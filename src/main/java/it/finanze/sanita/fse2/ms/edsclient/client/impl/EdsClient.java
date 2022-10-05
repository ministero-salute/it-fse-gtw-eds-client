package it.finanze.sanita.fse2.ms.edsclient.client.impl;

import it.finanze.sanita.fse2.ms.edsclient.client.IEdsClient;
import it.finanze.sanita.fse2.ms.edsclient.config.Constants;
import it.finanze.sanita.fse2.ms.edsclient.config.EdsCFG;
import it.finanze.sanita.fse2.ms.edsclient.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.IngestorRequestDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.response.DocumentResponseDTO;
import it.finanze.sanita.fse2.ms.edsclient.enums.ErrorLogEnum;
import it.finanze.sanita.fse2.ms.edsclient.enums.ProcessorOperationEnum;
import it.finanze.sanita.fse2.ms.edsclient.enums.ResultLogEnum;
import it.finanze.sanita.fse2.ms.edsclient.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.edsclient.logging.LoggerHelper;
import it.finanze.sanita.fse2.ms.edsclient.repository.entity.IniEdsInvocationETY;
import it.finanze.sanita.fse2.ms.edsclient.utility.JsonUtility;
import it.finanze.sanita.fse2.ms.edsclient.utility.RequestUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

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
    private transient LoggerHelper logger;

    @Autowired
    private EdsCFG edsCFG;

    @Override
    public Boolean dispatchAndSendData(IngestorRequestDTO ingestorRequestDTO) {
        final Date startingDate = new Date();
        log.debug("Calling EDS ingestion ep - START"); 
        log.debug("Operation: {}", ingestorRequestDTO.getOperation().getName());
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json"); 
        
        DocumentReferenceDTO requestBody = buildRequestBody(ingestorRequestDTO);
        HttpEntity<?> entity = new HttpEntity<>(requestBody, headers);

        String issuer = Constants.AppConstants.UNKNOWN_ISSUER;
        String documentType = Constants.AppConstants.UNKNOWN_DOCUMENT_TYPE;
        IniEdsInvocationETY ety = ingestorRequestDTO.getIniEdsInvocationETY() != null ? ingestorRequestDTO.getIniEdsInvocationETY() : null;
        if (ety != null && ety.getMetadata() != null) {
            issuer = RequestUtility.extractFieldFromToken(ety.getMetadata(), "iss");
            documentType = RequestUtility.extractFieldFromMetadata(ety.getMetadata(), "typeCodeName");
        }

        ResponseEntity<DocumentResponseDTO> response = null;
        final String url = edsCFG.getEdsIngestionHost() + "/v1/document" + buildRequestPath(ingestorRequestDTO.getOperation(), ingestorRequestDTO.getIdentifier());
        try {
            response = restTemplate.exchange(url, Constants.AppConstants.methodMap.get(ingestorRequestDTO.getOperation()), entity,
                    DocumentResponseDTO.class);
            log.debug("{} status returned from eds", response.getStatusCode());
            logger.info("Informazioni inviate all'Ingestion", ingestorRequestDTO.getOperation().getOperationLogEnum(), ResultLogEnum.OK, startingDate, issuer, documentType);
        } catch(Exception ex) {
            logger.error("Errore riscontrato durante l'invio delle informazioni all'Ingestion", ingestorRequestDTO.getOperation().getOperationLogEnum(), ResultLogEnum.KO, startingDate, ErrorLogEnum.KO_PUB, issuer, documentType);
            log.error("Generic error while call eds ingestion ep: " + ex);
            throw ex;
        }

        return response.getStatusCode().is2xxSuccessful();
    } 
    
    private DocumentReferenceDTO buildRequestBody(IngestorRequestDTO ingestorRequestDTO) {
        DocumentReferenceDTO requestBody = null;
        IniEdsInvocationETY ety = ingestorRequestDTO.getIniEdsInvocationETY() != null ? ingestorRequestDTO.getIniEdsInvocationETY() : null;

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
	        	requestBody = new DocumentReferenceDTO();
	            requestBody.setIdentifier(ingestorRequestDTO.getIdentifier());
	            requestBody.setOperation(ProcessorOperationEnum.REPLACE);
                if (ety != null && ety.getData() != null) {
                    requestBody.setJsonString(JsonUtility.objectToJson(ety.getData()));
                } else {
                    throw new BusinessException(MSG_UNSUPPORTED);
                }
                break;
	        	
	        case DELETE: 
	        	break;

            case PUBLISH:
	        default:
	        	requestBody = new DocumentReferenceDTO();
	            requestBody.setIdentifier(ingestorRequestDTO.getIdentifier());
	            requestBody.setOperation(ProcessorOperationEnum.PUBLISH);
                requestBody.setPriorityType(ingestorRequestDTO.getPriorityType());
                if (ety != null && ety.getData() != null) {
                    requestBody.setJsonString(JsonUtility.objectToJson(ety.getData()));
                } else {
                    throw new BusinessException(MSG_UNSUPPORTED);
                }
	        	break;
        } 
        
        return requestBody; 

    }
    
    private String buildRequestPath(ProcessorOperationEnum operation, String identifier) {
        String requestPath = "";

        switch(operation) {
            case UPDATE:
                requestPath = "/metadata";
                break;
            case DELETE:
                requestPath = "/identifier/"+ identifier;
                break;
            case PUBLISH:
            case REPLACE:
            default:
                break;
        }
        return requestPath;
    }
}
