package it.finanze.sanita.fse2.ms.edsclient.enums;

import lombok.Getter;

public enum ErrorLogEnum implements ILogEnum {

	KO_PUB("KO-PUB", "Errore nella pubblicazione del CDA"),
	KO_REPLACE("KO-REPLACE", "Errore nella replace del CDA"),
	KO_UPDATE("KO-UPDATE", "Errore nell'update del CDA"),
	KO_DELETE("KO-DELETE", "Errore nella delete del CDA");

	@Getter
	private String code;
	
	@Getter
	private String description;

	private ErrorLogEnum(String inCode, String inDescription) {
		code = inCode;
		description = inDescription;
	}

}
