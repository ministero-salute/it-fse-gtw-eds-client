package it.finanze.sanita.fse2.ms.edsclient.enums;

import lombok.Getter;

public enum TipoDocAltoLivEnum {

	WOR("WOR", "Documento di workflow"),
	REF("REF","Referto"),
	LDO("LDO","Lettera di dimissione ospedaliera"),
	RIC("RIC","Richiesta"),
	SUM("SUM","Sommario"),
	TAC("TAC","Taccuino"),
	PRS("PRS","Prescrizione"),
	PRE("PRE","Prestazioni"),
	ESE("ESE","Esenzione"),
	PDC("PDC","Piano di cura"),
	VAC("VAC","Vaccino"),
	CER("CER","Certificato per DGC"),
	VRB("VRB","Verbale");

	@Getter
	private String code;
	
	@Getter
	private String description;

	private TipoDocAltoLivEnum(String inCode, String inDescription) {
		code = inCode;
		description = inDescription;
	}

}
