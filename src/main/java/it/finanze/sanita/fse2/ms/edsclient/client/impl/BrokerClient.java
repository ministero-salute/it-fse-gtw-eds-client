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

import java.net.URI;
import java.util.Date;

import it.finanze.sanita.fse2.ms.edsclient.dto.response.GetIngestionStatusResponseDTO;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import it.finanze.sanita.fse2.ms.edsclient.client.IBrokerClient;
import it.finanze.sanita.fse2.ms.edsclient.config.BrokerCfg;
import it.finanze.sanita.fse2.ms.edsclient.config.Constants;
import it.finanze.sanita.fse2.ms.edsclient.dto.DocumentDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.EdsResponseDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.BrokerRequestDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.response.DocumentResponseDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.response.GetDocumentReferenceResDTO;
import it.finanze.sanita.fse2.ms.edsclient.enums.ProcessorOperationEnum;
import it.finanze.sanita.fse2.ms.edsclient.enums.ResultLogEnum;
import it.finanze.sanita.fse2.ms.edsclient.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.edsclient.logging.LoggerHelper;
import it.finanze.sanita.fse2.ms.edsclient.repository.entity.IniEdsInvocationETY;
import it.finanze.sanita.fse2.ms.edsclient.utility.JsonUtility;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BrokerClient implements IBrokerClient {

	private static final String MSG_UNSUPPORTED = "Unsupported exception";

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private LoggerHelper logger;

	@Autowired
	private BrokerCfg brokerCfg;

	@Override
	public EdsResponseDTO dispatchAndSendData(BrokerRequestDTO brokerRequestDTO) {

		EdsResponseDTO output = new EdsResponseDTO();
		final Date startingDate = new Date();
		final String successLog = "Informazioni inviate al broker";
		final String errorLog = "Errore riscontrato durante l'invio delle informazioni al broker";

		URI url = UriComponentsBuilder
				.fromUriString(brokerCfg.getBrokerHost() + "/edsalim/v1/ingestion/document"
						+ buildRequestPath(brokerRequestDTO.getOperation(),
								brokerRequestDTO.getIdentifier(), brokerRequestDTO.getWorkflowInstanceId(),
								brokerRequestDTO.getFiscalCode()))
				.build().toUri();

		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
		headers.set(Constants.AppConstants.X_SUBJECT_ROLE_HEADER, Constants.AppConstants.SUBJECT_ROLE_GTW);

		try {
			DocumentDTO requestBody = buildRequestBody(brokerRequestDTO);
			HttpEntity<?> entity = new HttpEntity<>(requestBody, headers);

			restTemplate.exchange(url, Constants.AppConstants.methodMap.get(brokerRequestDTO.getOperation()), entity,
					DocumentResponseDTO.class);

			logger.info(successLog, brokerRequestDTO.getOperation().getOperationLogEnum(), ResultLogEnum.OK,
					startingDate);
			output.setEsito(true);
		} catch (Exception ex) {
			logger.error(errorLog, brokerRequestDTO.getOperation().getOperationLogEnum(), ResultLogEnum.KO,
					startingDate, brokerRequestDTO.getOperation().getErrorLogEnum());
			output.setExClassCanonicalName(ExceptionUtils.getRootCause(ex).getClass().getCanonicalName());
			output.setMessageError(ex.getMessage());
		}

		return output;
	}

	private DocumentDTO buildRequestBody(BrokerRequestDTO brokerRequestDTO) {
		DocumentDTO requestBody = null;
		IniEdsInvocationETY ety = brokerRequestDTO.getIniEdsInvocationETY() != null
				? brokerRequestDTO.getIniEdsInvocationETY()
						: null;

		switch (brokerRequestDTO.getOperation()) {
		case UPDATE:
			if (brokerRequestDTO.getUpdateReqDTO() == null) {
				// bad request
				throw new BusinessException(MSG_UNSUPPORTED);
			}
			requestBody = new DocumentDTO();
			requestBody.setIdentifier(brokerRequestDTO.getIdentifier());
			requestBody.setOperation(ProcessorOperationEnum.UPDATE);
			requestBody.setJsonString(brokerRequestDTO.getUpdateReqDTO().getDocumentReference());
			requestBody.setFiscalCode(brokerRequestDTO.getFiscalCode());
			//                requestBody.setRde(brokerRequestDTO.getIniEdsInvocationETY().getRde());
			break;
		case REPLACE:
			requestBody = new DocumentDTO();
			requestBody.setIdentifier(brokerRequestDTO.getIdentifier());
			requestBody.setOperation(ProcessorOperationEnum.REPLACE);
			requestBody.setFiscalCode(brokerRequestDTO.getIniEdsInvocationETY().getFiscalCode());
			requestBody.setRde(brokerRequestDTO.getIniEdsInvocationETY().getRde());
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
			requestBody = new DocumentDTO();
			requestBody.setIdentifier(brokerRequestDTO.getIdentifier());
			requestBody.setOperation(ProcessorOperationEnum.PUBLISH);
			requestBody.setFiscalCode(brokerRequestDTO.getIniEdsInvocationETY().getFiscalCode());
			requestBody.setRde(brokerRequestDTO.getIniEdsInvocationETY().getRde());
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
			final String workflowInstanceId,
			final String fiscalCode) {
		String requestPath = "";

		switch (operation) {
		case UPDATE:
			requestPath = "/metadata";
			break;
		case DELETE:
			requestPath = "/identifier/" + identifier + "/" + fiscalCode;
			break;
		case REPLACE:
		case PUBLISH:
			requestPath = "/workflowinstanceid/" + workflowInstanceId;
			break;
		default:
			break;
		}
		return requestPath;
	}

	@Override
	public GetDocumentReferenceResDTO getDocumentReference(String fiscalCode, String masterIdentifier) {
		final URI uri = UriComponentsBuilder
				.fromUriString(brokerCfg.getBrokerHost())
				.path("/v1/ingestion/document-reference/{fiscalCode}/{masterIdentifier}")
				.buildAndExpand(fiscalCode, masterIdentifier)
				.toUri();

		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
		headers.set(Constants.AppConstants.X_SUBJECT_ROLE_HEADER, Constants.AppConstants.SUBJECT_ROLE_GTW);

		HttpEntity<Void> entity = new HttpEntity<>(headers);

		return restTemplate.exchange(uri, org.springframework.http.HttpMethod.GET, entity, GetDocumentReferenceResDTO.class).getBody();
	}

    @Override
    public GetIngestionStatusResponseDTO getIngestionStatus(String workflowInstanceId) {
        log.debug("BrokerClient - Calling broker to retrieve ingestion status");

        URI url = UriComponentsBuilder
                .fromUriString(brokerCfg.getBrokerHost())
                .path("edsalim/v1/ingestion/status/{workflowInstanceId}")
                .encode()
                .buildAndExpand(workflowInstanceId)
                .toUri();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set(Constants.AppConstants.X_SUBJECT_ROLE_HEADER, Constants.AppConstants.SUBJECT_ROLE_GTW);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            GetIngestionStatusResponseDTO response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    GetIngestionStatusResponseDTO.class).getBody();

            log.debug("BrokerClient - Ingestion status retrieved successfully");
            return response;
        } catch (Exception ex) {
            log.error("Error calling broker getIngestionStatus API: {}", ex.getMessage(), ex);
            throw new BusinessException("Error calling broker getIngestionStatus API", ex);
        }
    }


}
