package it.finanze.sanita.fse2.ms.edsclient.nce.dto;

import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import it.finanze.sanita.fse2.ms.edsclient.nce.enums.StoreTypeEnum;
import it.finanze.sanita.fse2.ms.edsclient.nce.enums.TransformationEnum;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KeyPairDto {

	private String kid;

	private Date notAfter;
	private Integer keySize;
	private String keyAlg;
	private String issuer;
	private String serialNumber;

	private Map<StoreTypeEnum, PrivateKey> privateKeyFormats;
	private Certificate pubKeyHolder;

	private Collection<String> scopes;
	private TransformationEnum defautlTrasformation;

	@Override
	public String toString() {
		return "KID\t\t" + kid + "\n" +
			    "NOT AFTER\t" + notAfter + "\n" +
			    "SIZE\t\t" + keySize + "\n" +
			    "ALG\t\t" + keyAlg + "\n" +
			    "ISSUER\t\t" + issuer + "\n" +
			    "SERIAL NUMBER\t" + serialNumber + "\n" +
			    "PUBLIC PRESENT\t" + (pubKeyHolder!=null) + "\n" +
			    "PRIVATE FORMATS\t" + privateKeyFormats.keySet() + "\n" +
			    "SCOPES\t\t" + scopes + "\n" +
			    "DEFAULT TX\t" + defautlTrasformation + "\n";
	}
	
}
