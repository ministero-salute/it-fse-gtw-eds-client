/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.edsclient.logging;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import it.finanze.sanita.fse2.ms.edsclient.client.IConfigClient;
import it.finanze.sanita.fse2.ms.edsclient.dto.LogDTO;
import it.finanze.sanita.fse2.ms.edsclient.enums.ILogEnum;
import it.finanze.sanita.fse2.ms.edsclient.enums.ResultLogEnum;
import lombok.extern.slf4j.Slf4j;

/** 
 * @author vincenzoingenito 
 */ 
@Service
@Slf4j
public class LoggerHelper {
    
	Logger kafkaLog = LoggerFactory.getLogger("kafka-logger"); 
	
	@Value("${log.kafka-log.enable}")
	private boolean kafkaLogEnable;
	
	@Autowired
	private IConfigClient configClient;
	
	private String gatewayName;
	
	@Value("${spring.application.name}")
	private String msName;

	/* 
	 * Specify here the format for the dates 
	 */
	private DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS"); 
	
	
	/* 
	 * Implements structured logs, at all logging levels
	 */
	public void trace(String message, ILogEnum operation, ResultLogEnum result, 
		Date startDateOperation, String issuer, String documentType, String subjectRole) {
		
		final String gatewayName = getGatewayName();

		LogDTO logDTO = LogDTO.builder().
				op_issuer(issuer).
				message(message).
				operation(operation.getCode()).
				op_role(subjectRole).
				op_result(result.getCode()).
				op_timestamp_start(dateFormat.format(startDateOperation)).
				op_timestamp_end(dateFormat.format(new Date())).
				gateway_name(gatewayName).
				microservice_name(msName).
				build();

		final String logMessage = new Gson().toJson(logDTO);
		log.trace(logMessage);

		if (Boolean.TRUE.equals(kafkaLogEnable)) {
			kafkaLog.trace(logMessage);
		}
	} 
	
	public void debug(String message,  ILogEnum operation, ResultLogEnum result, 
		Date startDateOperation, String issuer, String documentType, String subjectRole) {
		
		final String gatewayName = getGatewayName();
		
		LogDTO logDTO = LogDTO.builder().
				op_issuer(issuer).
				message(message).
				operation(operation.getCode()).
				op_role(subjectRole).
				op_result(result.getCode()).
				op_timestamp_start(dateFormat.format(startDateOperation)).
				op_timestamp_end(dateFormat.format(new Date())).
				gateway_name(gatewayName).
				microservice_name(msName).
				build();
		
		final String logMessage = new Gson().toJson(logDTO);
		log.debug(logMessage);

		if (Boolean.TRUE.equals(kafkaLogEnable)) {
			kafkaLog.debug(logMessage);
		}
	} 
	 
	public void info(String message, ILogEnum operation, ResultLogEnum result, 
		Date startDateOperation, String issuer, String documentType, String subjectRole) {
		
		final String gatewayName = getGatewayName();
		
		LogDTO logDTO = LogDTO.builder().
				op_issuer(issuer).
				message(message).
				operation(operation.getCode()).
				op_role(subjectRole).
				op_result(result.getCode()).
				op_timestamp_start(dateFormat.format(startDateOperation)).
				op_timestamp_end(dateFormat.format(new Date())).
				gateway_name(gatewayName).
				microservice_name(msName).
				build();
		
		final String logMessage = new Gson().toJson(logDTO);
		log.info(logMessage);
		if (Boolean.TRUE.equals(kafkaLogEnable)) {
			kafkaLog.info(logMessage);
		}
	} 
	
	public void warn(String message, ILogEnum operation, ResultLogEnum result, 
		Date startDateOperation, String issuer, String documentType, String subjectRole) {
		
		final String gatewayName = getGatewayName();
		LogDTO logDTO = LogDTO.builder().
				op_issuer(issuer).
				message(message).
				operation(operation.getCode()).
				op_role(subjectRole).
				op_result(result.getCode()).
				op_timestamp_start(dateFormat.format(startDateOperation)).
				op_timestamp_end(dateFormat.format(new Date())).
				gateway_name(gatewayName).
				microservice_name(msName).
				build();
		
		final String logMessage = new Gson().toJson(logDTO);
		log.warn(logMessage);
 
		if (Boolean.TRUE.equals(kafkaLogEnable)) {
			kafkaLog.warn(logMessage);
		}
 
	} 
	
	public void error(String message, ILogEnum operation, ResultLogEnum result, 
		Date startDateOperation, ILogEnum error, String issuer, String documentType, String subjectRole) {
		
		final String gatewayName = getGatewayName();

		LogDTO logDTO = LogDTO.builder().
				op_issuer(issuer).
				message(message).
				operation(operation.getCode()).
				op_role(subjectRole).
				op_result(result.getCode()).
				op_timestamp_start(dateFormat.format(startDateOperation)).
				op_timestamp_end(dateFormat.format(new Date())).
				op_error(error.getCode()).
				op_error_description(error.getDescription()).
				gateway_name(gatewayName).
				microservice_name(msName).
				build();
		
		final String logMessage = new Gson().toJson(logDTO);
		log.error(logMessage);

		if (Boolean.TRUE.equals(kafkaLogEnable)) {
			kafkaLog.error(logMessage);
		}
		
	}
    
	/**
	 * Returns the gateway name.
	 * 
	 * @return The GatewayName of the ecosystem.
	 */
	private String getGatewayName() {
		if (gatewayName == null) {
			gatewayName = configClient.getGatewayName();
		}
		return gatewayName;
	}

}
