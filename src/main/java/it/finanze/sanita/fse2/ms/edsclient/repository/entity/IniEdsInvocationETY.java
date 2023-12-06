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
package it.finanze.sanita.fse2.ms.edsclient.repository.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;


/**
 * Model to save ini and eds invocation info.
 */
@Document(collection = "#{@iniEdsInvocationBean}")
@Data
@NoArgsConstructor
public class IniEdsInvocationETY {

	public static final String FIELD_WIF = "workflow_instance_id";
	public static final String FIELD_DATA = "data";
	public static final String FIELD_METADATA = "metadata";

	@Id
	private String id;
	
	@Field(name = FIELD_WIF)
	private String workflowInstanceId;
	
	@Field(name = FIELD_DATA)
	private org.bson.Document data;
	
	@Field(name = FIELD_METADATA)
	private List<org.bson.Document> metadata;
	 
}