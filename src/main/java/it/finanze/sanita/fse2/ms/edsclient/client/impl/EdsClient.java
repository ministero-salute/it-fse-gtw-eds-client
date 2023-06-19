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
package it.finanze.sanita.fse2.ms.edsclient.client.impl;

import java.util.Date;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import it.finanze.sanita.fse2.ms.edsclient.client.IEdsClient;
import it.finanze.sanita.fse2.ms.edsclient.config.Constants;
import it.finanze.sanita.fse2.ms.edsclient.config.EdsCFG;
import it.finanze.sanita.fse2.ms.edsclient.dto.DocumentReferenceDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.EdsResponseDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.IngestorRequestDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.response.DocumentResponseDTO;
import it.finanze.sanita.fse2.ms.edsclient.enums.ProcessorOperationEnum;
import it.finanze.sanita.fse2.ms.edsclient.enums.ResultLogEnum;
import it.finanze.sanita.fse2.ms.edsclient.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.edsclient.logging.LoggerHelper;
import it.finanze.sanita.fse2.ms.edsclient.repository.entity.IniEdsInvocationETY;
import it.finanze.sanita.fse2.ms.edsclient.utility.JsonUtility;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EdsClient implements IEdsClient {

    private static final String MSG_UNSUPPORTED = "Unsupported exception";
 
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private LoggerHelper logger;

    @Autowired
    private EdsCFG edsCFG;

    @Override
    public EdsResponseDTO dispatchAndSendData(IngestorRequestDTO ingestorRequestDTO) {
    	EdsResponseDTO output = new EdsResponseDTO();
    	final Date startingDate = new Date();
    	try {
    		log.debug("Calling EDS ingestion ep - START"); 
    		log.debug("Operation: {}", ingestorRequestDTO.getOperation().getName());

    		HttpHeaders headers = new HttpHeaders();
    		headers.set("Content-Type", "application/json"); 

    		DocumentReferenceDTO requestBody = buildRequestBody(ingestorRequestDTO);
    		HttpEntity<?> entity = new HttpEntity<>(requestBody, headers);

    		final String url = edsCFG.getEdsIngestionHost() + "/v1/document" + buildRequestPath(ingestorRequestDTO.getOperation(), ingestorRequestDTO.getIdentifier(),
    				ingestorRequestDTO.getWorkflowInstanceId());

    		restTemplate.exchange(url, Constants.AppConstants.methodMap.get(ingestorRequestDTO.getOperation()), entity, DocumentResponseDTO.class);
    		logger.info("Informazioni inviate all'Ingestion", ingestorRequestDTO.getOperation().getOperationLogEnum(), ResultLogEnum.OK, startingDate);
    		output.setEsito(true);
    	} catch(Exception ex) {
    		logger.error("Errore riscontrato durante l'invio delle informazioni all'Ingestion", ingestorRequestDTO.getOperation().getOperationLogEnum(), ResultLogEnum.KO, startingDate, ingestorRequestDTO.getOperation().getErrorLogEnum());
    		output.setExClassCanonicalName(ExceptionUtils.getRootCause(ex).getClass().getCanonicalName());
    		output.setMessageError(ex.getMessage());
    	}

    	return output;
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
    
    private String buildRequestPath(final ProcessorOperationEnum operation, final String identifier,
    		final String workflowInstanceId) {
        String requestPath = "";

        switch(operation) {
            case UPDATE:
                requestPath = "/metadata";
                break;
            case DELETE:
                requestPath = "/identifier/"+ identifier;
                break;
            case REPLACE:
            case PUBLISH:
            	requestPath = "/workflowinstanceid/"+ workflowInstanceId;
            	break;
            default:
                break;
        }
        return requestPath;
    }
}
