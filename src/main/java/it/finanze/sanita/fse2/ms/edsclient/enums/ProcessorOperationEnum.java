package it.finanze.sanita.fse2.ms.edsclient.enums;

public enum ProcessorOperationEnum {

	CREATE("Create"),
	UPDATE("Update"),
	DELETE("Delete"); 

	private final String name;

	public String getName() {
		return name;
	}
	ProcessorOperationEnum(String pname) {
		name = pname;
	}
}
