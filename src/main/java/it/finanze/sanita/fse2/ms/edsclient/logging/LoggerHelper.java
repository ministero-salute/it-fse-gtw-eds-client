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
package it.finanze.sanita.fse2.ms.edsclient.logging;

import com.google.gson.Gson;
import it.finanze.sanita.fse2.ms.edsclient.dto.LogDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.OptionalLogDataDTO;
import it.finanze.sanita.fse2.ms.edsclient.client.IConfigClient;
import it.finanze.sanita.fse2.ms.edsclient.enums.ILogEnum;
import it.finanze.sanita.fse2.ms.edsclient.enums.ResultLogEnum;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
 
@Service
@Slf4j
public class LoggerHelper {

	private static final Logger KAFKA_LOGGER = LoggerFactory.getLogger("kafka-logger");
	private DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS"); 

	private final IConfigClient configClient;
	private String gatewayName;

	public LoggerHelper(IConfigClient configClient) {
		this.configClient = configClient;
	}

	@Value("${spring.application.name}")
	private String msName;
	
	public void info(String message, ILogEnum operation, ResultLogEnum result, 
		Date startDateOperation, OptionalLogDataDTO optionalData) {
		final String logMessage = new Gson().toJson(buildLog(message, operation, result,
				startDateOperation, optionalData, null));
		log.info(logMessage);
		KAFKA_LOGGER.info(logMessage);
	}
	
	public void error(String message, ILogEnum operation, ResultLogEnum result, 
		Date startDateOperation, ILogEnum error, OptionalLogDataDTO optionalData) {
		final String logMessage = new Gson().toJson(buildLog(message, operation, result,
				startDateOperation, optionalData, error));
		log.error(logMessage);
		KAFKA_LOGGER.error(logMessage);
	}

	private LogDTO buildLog(String message, ILogEnum operation, ResultLogEnum result,
			Date startDateOperation, OptionalLogDataDTO data, ILogEnum error) {
		return LogDTO.builder()
				.message(message).operation(operation.getCode()).op_result(result.getCode())
				.microservice_name(msName)
				.gateway_name(getGatewayName())
				.op_timestamp_start(dateFormat.format(startDateOperation))
				.op_timestamp_end(dateFormat.format(new Date()))
				.op_error(error == null ? null : error.getCode())
				.op_error_description(error == null ? null : error.getDescription())
				.op_issuer(data == null ? null : data.getIssuer())
				.op_locality(data == null ? null : data.getLocality())
				.op_role(data == null ? null : data.getRole())
				.op_fiscal_code(data == null ? null : data.getFiscalCode())
				.op_document_type(data == null ? null : data.getDocumentType())
				.workflow_instance_id(data == null ? null : data.getWorkflowInstanceId())
				.idDocumento(data == null ? null : data.getDocumentId())
				.op_application_id(data == null ? null : data.getApplicationId())
				.op_application_vendor(data == null ? null : data.getApplicationVendor())
				.op_application_version(data == null ? null : data.getApplicationVersion())
				.build();
	}

	private String getGatewayName() {
		if (gatewayName == null) {
			try {
				gatewayName = configClient.getGatewayName();
			} catch (RuntimeException ex) {
				log.warn("Unable to retrieve gateway name for structured log", ex);
			}
		}
		return gatewayName;
	}
}
