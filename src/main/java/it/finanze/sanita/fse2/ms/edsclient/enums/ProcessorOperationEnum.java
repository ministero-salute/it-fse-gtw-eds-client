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
package it.finanze.sanita.fse2.ms.edsclient.enums;

import lombok.Getter;

public enum ProcessorOperationEnum {

	PUBLISH("PUBLISH", OperationLogEnum.PUB_CDA2, ErrorLogEnum.KO_PUB),
	DELETE("DELETE", OperationLogEnum.DELETE_CDA2, ErrorLogEnum.KO_DELETE),
	REPLACE("REPLACE", OperationLogEnum.REPLACE_CDA2, ErrorLogEnum.KO_REPLACE),
	UPDATE("UPDATE", OperationLogEnum.UPDATE_CDA, ErrorLogEnum.KO_UPDATE);

	@Getter
	private final String name;

	@Getter
	private final OperationLogEnum operationLogEnum;

	@Getter
	private final ErrorLogEnum errorLogEnum;

	ProcessorOperationEnum(String pname, OperationLogEnum logType, ErrorLogEnum errorLogEnum) {
		name = pname;
		operationLogEnum = logType;
		this.errorLogEnum = errorLogEnum;
	}

}
