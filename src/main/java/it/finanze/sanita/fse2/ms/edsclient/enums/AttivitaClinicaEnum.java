package it.finanze.sanita.fse2.ms.edsclient.enums;

import lombok.Getter;

public enum AttivitaClinicaEnum {

	PHR("PHR", "Personal Health Record Update"),
	CON("CON", "Consulto"),
	DIS("DIS", "Discharge"),
	ERP("ERP", "Erogazione Prestazione Prenotata"),
	Sistema_TS("Sistema TS", "Documenti sistema TS");

	@Getter
	private String code;
	
	@Getter
	private String description;

	private AttivitaClinicaEnum(String inCode, String inDescription) {
		code = inCode;
		description = inDescription;
	}

}
