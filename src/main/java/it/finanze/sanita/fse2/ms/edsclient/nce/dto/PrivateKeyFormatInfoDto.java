package it.finanze.sanita.fse2.ms.edsclient.nce.dto;

import it.finanze.sanita.fse2.ms.edsclient.nce.enums.StoreTypeEnum;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PrivateKeyFormatInfoDto {

	private StoreTypeEnum storageType;
	private String alias;
	private char[] pwd;
	private String privKeyPath;

}
