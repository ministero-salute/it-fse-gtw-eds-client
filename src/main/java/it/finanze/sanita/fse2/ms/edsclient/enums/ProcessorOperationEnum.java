package it.finanze.sanita.fse2.ms.edsclient.enums;

import lombok.Getter;

public enum ProcessorOperationEnum {

	PUBLISH("PUBLISH", OperationLogEnum.PUB_CDA2),
	DELETE("DELETE", OperationLogEnum.DELETE_CDA2),
	REPLACE("REPLACE", OperationLogEnum.REPLACE_CDA2),
	UPDATE("UPDATE", OperationLogEnum.UPDATE_CDA);

	@Getter
	private final String name;

	@Getter
	private final OperationLogEnum operationLogEnum;

	ProcessorOperationEnum(String pname, OperationLogEnum logType) {
		name = pname;
		operationLogEnum = logType;
	}

}
