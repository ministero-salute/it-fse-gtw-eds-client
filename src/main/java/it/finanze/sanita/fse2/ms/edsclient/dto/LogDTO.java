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
package it.finanze.sanita.fse2.ms.edsclient.dto;

import it.finanze.sanita.fse2.ms.edsclient.config.Constants;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class LogDTO {

	final String log_type = Constants.AppConstants.LOG_TYPE_CONTROL;
	
	private String message;
	
	private String operation;
	
	private String op_result;
	
	private String op_timestamp_start;
	
	private String op_timestamp_end;
	
	private String op_error;
	
	private String op_error_description;

	private String op_issuer;

	private String op_locality;

	private String op_role;

	private String op_fiscal_code;

	private String op_document_type;

	private String workflow_instance_id;

	private String idDocumento;

	private String op_application_id;

	private String op_application_vendor;

	private String op_application_version;
	
	private String gateway_name;
	
	private String microservice_name;
}
