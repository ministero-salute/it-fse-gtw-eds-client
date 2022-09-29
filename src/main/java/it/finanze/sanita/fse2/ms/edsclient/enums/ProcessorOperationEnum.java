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
}
