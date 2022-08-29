package it.finanze.sanita.fse2.ms.edsclient.enums;

public enum HealthcareFacilityEnum {

	Ospedale("Ospedale"),
	Prevenzione("Prevenzione"),
	Territorio("Territorio"),
	SistemaTS("SistemaTS"),
	Cittadino("Cittadino");

	private String code;

	private HealthcareFacilityEnum(String inCode) {
		code = inCode;
	}

	public String getCode() {
		return code;
	}

}