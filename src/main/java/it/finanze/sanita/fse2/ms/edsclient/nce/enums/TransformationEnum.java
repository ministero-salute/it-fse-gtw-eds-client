package it.finanze.sanita.fse2.ms.edsclient.nce.enums;

import lombok.Getter;

@Getter
public enum TransformationEnum {

	RSA("RSA/ECB/PKCS1Padding"), AES("AES/CBC/PKCS5Padding"), DES("DES/ECB/PKCS5Padding");

	private String trasformation;

	private TransformationEnum(String inTrasformation) {
		trasformation = inTrasformation;
	}
	
	static public TransformationEnum get(String tx) {
		TransformationEnum out = null;
		for (TransformationEnum e:TransformationEnum.values()) {
			if (e.getTrasformation().equals(tx)) {
				out = e;
				break;
			}
		}
		return out;
	}
	
}
