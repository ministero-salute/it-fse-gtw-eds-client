/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
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
