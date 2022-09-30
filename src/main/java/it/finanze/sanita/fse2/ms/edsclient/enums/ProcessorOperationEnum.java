package it.finanze.sanita.fse2.ms.edsclient.enums;

import lombok.Getter;

public enum ProcessorOperationEnum {

	PUBLISH("PUBLISH"),
	DELETE("DELETE"),
	REPLACE("REPLACE"),
	UPDATE("UPDATE");

	@Getter
	private final String name;

	ProcessorOperationEnum(String pname) {
		name = pname;
	}

	public ILogEnum toLogOperation() {
		switch (this) {
		case PUBLISH:
			return OperationLogEnum.PUB_CDA2;
		case DELETE:
			return OperationLogEnum.DELETE_CDA2;
		case REPLACE:
			return OperationLogEnum.REPLACE_CDA2;
		case UPDATE:
			return OperationLogEnum.UPDATE_CDA;
		default:
			return null;
		}
	}
}
