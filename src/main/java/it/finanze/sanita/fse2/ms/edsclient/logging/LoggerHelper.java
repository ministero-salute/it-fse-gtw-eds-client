/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.edsclient.logging;

import com.google.gson.Gson;
import it.finanze.sanita.fse2.ms.edsclient.dto.LogDTO;
import it.finanze.sanita.fse2.ms.edsclient.enums.ILogEnum;
import it.finanze.sanita.fse2.ms.edsclient.enums.ResultLogEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
 
@Service
@Slf4j
public class LoggerHelper {

	/* 
	 * Specify here the format for the dates 
	 */
	private DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS"); 
	
	
	/* 
	 * Implements structured logs, at all logging levels
	 */
	public void trace(String message, ILogEnum operation, ResultLogEnum result, 
		Date startDateOperation) {


		LogDTO logDTO = LogDTO.builder().
				message(message).
				operation(operation.getCode()).
				op_result(result.getCode()).
				op_timestamp_start(dateFormat.format(startDateOperation)).
				op_timestamp_end(dateFormat.format(new Date())).
				build();

		final String logMessage = new Gson().toJson(logDTO);
		log.trace(logMessage);
	} 
	
	public void debug(String message,  ILogEnum operation, ResultLogEnum result, 
		Date startDateOperation) {

		
		LogDTO logDTO = LogDTO.builder().
				message(message).
				operation(operation.getCode()).
				op_result(result.getCode()).
				op_timestamp_start(dateFormat.format(startDateOperation)).
				op_timestamp_end(dateFormat.format(new Date())).
				build();
		
		final String logMessage = new Gson().toJson(logDTO);
		log.debug(logMessage);
	} 
	 
	public void info(String message, ILogEnum operation, ResultLogEnum result, 
		Date startDateOperation) {

		
		LogDTO logDTO = LogDTO.builder().
				message(message).
				operation(operation.getCode()).
				op_result(result.getCode()).
				op_timestamp_start(dateFormat.format(startDateOperation)).
				op_timestamp_end(dateFormat.format(new Date())).
				build();
		
		final String logMessage = new Gson().toJson(logDTO);
		log.info(logMessage);
	} 
	
	public void warn(String message, ILogEnum operation, ResultLogEnum result, 
		Date startDateOperation) {

		LogDTO logDTO = LogDTO.builder().
				message(message).
				operation(operation.getCode()).
				op_result(result.getCode()).
				op_timestamp_start(dateFormat.format(startDateOperation)).
				op_timestamp_end(dateFormat.format(new Date())).
				build();
		
		final String logMessage = new Gson().toJson(logDTO);
		log.warn(logMessage);
 
	} 
	
	public void error(String message, ILogEnum operation, ResultLogEnum result, 
		Date startDateOperation, ILogEnum error) {


		LogDTO logDTO = LogDTO.builder().
				message(message).
				operation(operation.getCode()).
				op_result(result.getCode()).
				op_timestamp_start(dateFormat.format(startDateOperation)).
				op_timestamp_end(dateFormat.format(new Date())).
				op_error(error.getCode()).
				op_error_description(error.getDescription()).
				build();
		
		final String logMessage = new Gson().toJson(logDTO);
		log.error(logMessage);
		
	}
}
