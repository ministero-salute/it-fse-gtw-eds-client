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
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import it.finanze.sanita.fse2.ms.edsclient.client.IBrokerClient;
import it.finanze.sanita.fse2.ms.edsclient.config.BrokerCfg;
import it.finanze.sanita.fse2.ms.edsclient.dto.DocumentDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.EdsResponseDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.OptionalLogDataDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.EdsMetadataUpdateReqDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.response.DocumentResponseDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.response.GetDocumentReferenceResDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.response.GetIngestionStatusResponseDTO;
import it.finanze.sanita.fse2.ms.edsclient.enums.OperationLogEnum;
import it.finanze.sanita.fse2.ms.edsclient.enums.ProcessorOperationEnum;
import it.finanze.sanita.fse2.ms.edsclient.enums.ResultLogEnum;
import it.finanze.sanita.fse2.ms.edsclient.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.edsclient.logging.LoggerHelper;
import it.finanze.sanita.fse2.ms.edsclient.repository.entity.IniEdsInvocationETY;
import it.finanze.sanita.fse2.ms.edsclient.utility.JsonUtility;
import it.finanze.sanita.fse2.ms.edsclient.utility.JwtUtility;
import it.finanze.sanita.fse2.ms.edsclient.utility.RequestUtility;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BrokerClient implements IBrokerClient {

	private static final String BASE_DOCUMENT_PATH = "/edsalim/v1/ingestion/document";

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private LoggerHelper logger;

	@Autowired
	private BrokerCfg brokerCfg;

	@Autowired
	private JwtUtility jwtUtility;

	// -------------------------------------------------------------------------
	// Operazioni su documento (PUBLISH / REPLACE / UPDATE / DELETE)
	// -------------------------------------------------------------------------

	@Override
	public EdsResponseDTO publish(String idDoc, String workflowInstanceId, IniEdsInvocationETY ety,
			OptionalLogDataDTO logData) {
		URI url = documentUri("/workflowinstanceid/" + workflowInstanceId);
		DocumentDTO body = buildEtyBody(idDoc, ProcessorOperationEnum.PUBLISH, ety);
		HttpHeaders headers = jwtHeadersFromMongo(ety);
		return executeBrokerCall(HttpMethod.POST, url, body, headers, logData,
				ProcessorOperationEnum.PUBLISH.getErrorLogEnum());
	}

	@Override
	public EdsResponseDTO replace(String idDoc, String workflowInstanceId, IniEdsInvocationETY ety,
			OptionalLogDataDTO logData) {
		URI url = documentUri("/workflowinstanceid/" + workflowInstanceId);
		DocumentDTO body = buildEtyBody(idDoc, ProcessorOperationEnum.REPLACE, ety);
		HttpHeaders headers = jwtHeadersFromMongo(ety);
		return executeBrokerCall(HttpMethod.PUT, url, body, headers, logData,
				ProcessorOperationEnum.REPLACE.getErrorLogEnum());
	}

	@Override
	public EdsResponseDTO update(String idDoc, EdsMetadataUpdateReqDTO updateReqDTO, String fiscalCode, String jwt) {
		URI url = documentUri("/metadata");
		DocumentDTO body = new DocumentDTO();
		body.setIdentifier(idDoc);
		body.setOperation(ProcessorOperationEnum.UPDATE);
		body.setJsonString(updateReqDTO.getDocumentReference());
		body.setFiscalCode(fiscalCode);
		HttpHeaders headers = jwtHeadersFromRequest(jwt);
		return executeBrokerCall(HttpMethod.PUT, url, body, headers, null,
				ProcessorOperationEnum.UPDATE.getErrorLogEnum());
	}

	@Override
	public EdsResponseDTO delete(String identifier, String fiscalCode, String jwt) {
		URI url = documentUri(UriComponentsBuilder.newInstance()
				.path("/identifier/{id}/{cf}")
				.buildAndExpand(identifier, fiscalCode)
				.toUriString());
		HttpHeaders headers = jwtHeadersFromRequest(jwt);
		return executeBrokerCall(HttpMethod.DELETE, url, null, headers, null,
				ProcessorOperationEnum.DELETE.getErrorLogEnum());
	}

	// -------------------------------------------------------------------------
	// Query
	// -------------------------------------------------------------------------

	@Override
	public GetDocumentReferenceResDTO getDocumentReference(String fiscalCode, String masterIdentifier, String jwt) {
		URI uri = UriComponentsBuilder
				.fromUriString(brokerCfg.getBrokerHost())
				.path("/edsalim/v1/ingestion/document-reference/{fiscalCode}/{masterIdentifier}")
				.buildAndExpand(fiscalCode, masterIdentifier)
				.toUri();

		if (jwt == null || jwt.isBlank()) {
			throw new BusinessException("Agid-JWT-Signature è obbligatorio per getDocumentReference");
		}
		HttpHeaders headers = jwtHeadersFromRequest(jwt);
		HttpEntity<Void> entity = new HttpEntity<>(headers);
		return restTemplate.exchange(uri, HttpMethod.GET, entity, GetDocumentReferenceResDTO.class).getBody();
	}

	@Override
	public GetIngestionStatusResponseDTO getIngestionStatus(String workflowInstanceId) {
		log.debug("BrokerClient - recupero ingestion status per {}", workflowInstanceId);

		URI url = UriComponentsBuilder
				.fromUriString(brokerCfg.getBrokerHost())
				.path("edsalim/v1/ingestion/status/{workflowInstanceId}")
				.encode()
				.buildAndExpand(workflowInstanceId)
				.toUri();
		try {
			HttpHeaders headers = jsonHeaders();
			HttpEntity<Void> entity = new HttpEntity<>(headers);
			GetIngestionStatusResponseDTO response = restTemplate
					.exchange(url, HttpMethod.GET, entity, GetIngestionStatusResponseDTO.class).getBody();
			log.debug("BrokerClient - ingestion status recuperato con successo");
			return response;
		} catch (Exception ex) {
			log.error("Errore chiamata broker getIngestionStatus: {}", ex.getMessage(), ex);
			throw new BusinessException("Errore chiamata broker getIngestionStatus", ex);
		}
	}

	// -------------------------------------------------------------------------
	// Metodi di supporto
	// -------------------------------------------------------------------------

	/**
	 * Esegue la chiamata HTTP verso il broker per le operazioni su documento,
	 * gestisce il logging strutturato e restituisce l'esito.
	 */
	private EdsResponseDTO executeBrokerCall(HttpMethod method, URI url, DocumentDTO body,
			HttpHeaders headers, OptionalLogDataDTO logData, it.finanze.sanita.fse2.ms.edsclient.enums.ErrorLogEnum errorLogEnum) {
		EdsResponseDTO output = new EdsResponseDTO();
		Date startingDate = new Date();
		try {
			HttpEntity<?> entity = new HttpEntity<>(body, headers);
			restTemplate.exchange(url, method, entity, DocumentResponseDTO.class);
			output.setEsito(true);
			logSuccess(startingDate, logData);
		} catch (Exception ex) {
			output.setExClassCanonicalName(ExceptionUtils.getRootCause(ex).getClass().getCanonicalName());
			output.setMessageError(ex.getMessage());
			logError("Errore invio informazioni al broker", startingDate, errorLogEnum, logData);
		}
		return output;
	}

	/** Costruisce il body per le operazioni PUBLISH/REPLACE che leggono da ETY (Mongo). */
	private DocumentDTO buildEtyBody(String idDoc, ProcessorOperationEnum op, IniEdsInvocationETY ety) {
		if (ety == null || ety.getData() == null) {
			throw new BusinessException("IniEdsInvocationETY o data assente per operazione " + op);
		}
		DocumentDTO body = new DocumentDTO();
		body.setIdentifier(idDoc);
		body.setOperation(op);
		body.setFiscalCode(ety.getFiscalCode());
		body.setRde(ety.getRde());
		body.setJsonString(JsonUtility.objectToJson(ety.getData()));
		return body;
	}

	/** Header con JWT ricavato dai metadati ETY salvati su Mongo (flusso PUBLISH/REPLACE). */
	private HttpHeaders jwtHeadersFromMongo(IniEdsInvocationETY ety) {
		Map<String, Object> claims = RequestUtility.extractJwtClaims(ety.getMetadata());
		HttpHeaders headers = jsonHeaders();
		headers.set("Agid-JWT-Signature", jwtUtility.buildTokenFromClaims(claims));
		return headers;
	}

	/** Header con JWT ri-pacchettizzato dal token in ingresso (flusso UPDATE/DELETE/GET). */
	private HttpHeaders jwtHeadersFromRequest(String inboundJwt) {
		HttpHeaders headers = jsonHeaders();
		headers.set("Agid-JWT-Signature", jwtUtility.buildTokenFromInboundJwt(inboundJwt));
		return headers;
	}

	/** Header base con solo Content-Type. */
	private HttpHeaders jsonHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
		return headers;
	}

	/** Costruisce la URI per gli endpoint sotto {@code /edsalim/v1/ingestion/document}. */
	private URI documentUri(String path) {
		return UriComponentsBuilder
				.fromUriString(brokerCfg.getBrokerHost() + BASE_DOCUMENT_PATH + path)
				.build().toUri();
	}

	private void logSuccess(Date startingDate, OptionalLogDataDTO logData) {
		try {
			logger.info(OperationLogEnum.SEND_TO_UAR.getDescription(), OperationLogEnum.SEND_TO_UAR,
					ResultLogEnum.OK, startingDate, logData);
		} catch (RuntimeException ex) {
			log.warn("Impossibile emettere log strutturato di successo verso broker", ex);
		}
	}

	private void logError(String message, Date startingDate,
			it.finanze.sanita.fse2.ms.edsclient.enums.ErrorLogEnum errorLogEnum, OptionalLogDataDTO logData) {
		try {
			logger.error(message, OperationLogEnum.SEND_TO_UAR, ResultLogEnum.KO,
					startingDate, errorLogEnum, logData);
		} catch (RuntimeException ex) {
			log.warn("Impossibile emettere log strutturato di errore verso broker", ex);
		}
	}
}
