/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.edsclient.enums;

import lombok.Getter;

public enum AttivitaClinicaEnum {

	PHR("PHR", "Personal Health Record Update"),
	CON("CON", "Consulto"),
	DIS("DIS", "Discharge"),
	ERP("ERP", "Erogazione Prestazione Prenotata"),
	Sistema_TS("Sistema TS", "Documenti sistema TS"),
	INI("INI","Documenti INI"),
	PN_DGC("PN-DGC","Documenti PN-DGC"),
	OBS("OBS","Documento stato di salute");

	@Getter
	private String code;
	
	@Getter
	private String description;

	private AttivitaClinicaEnum(String inCode, String inDescription) {
		code = inCode;
		description = inDescription;
	}

}
