/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.edsclient.enums;

public enum OperationLogEnum implements ILogEnum {

	PUB_CDA2("PUB-CDA2", "Pubblicazione CDA2"),
	DELETE_CDA2("DELETE-CDA2", "Cancellazione CDA2"),
	REPLACE_CDA2("REPLACE-CDA2", "Replace CDA2"),
	UPDATE_CDA("UPDATE-CDA2", "Update CDA2"); 

	private String code;
	
	public String getCode() {
		return code;
	}

	private String description;

	private OperationLogEnum(String inCode, String inDescription) {
		code = inCode;
		description = inDescription;
	}

	public String getDescription() {
		return description;
	}

}

