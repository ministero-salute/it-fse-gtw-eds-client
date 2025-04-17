package it.finanze.sanita.fse2.ms.edsclient.nce;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import it.finanze.sanita.fse2.ms.edsclient.nce.core.CertificateManager;
import it.finanze.sanita.fse2.ms.edsclient.nce.dto.PrivateKeyFormatInfoDto;
import it.finanze.sanita.fse2.ms.edsclient.nce.enums.StoreTypeEnum;
import it.finanze.sanita.fse2.ms.edsclient.nce.enums.TransformationEnum;


class ScenarioTest {
	
	private static final String privKeyPath = "/Users/vincenzoingenito/eclipse-workspace/it-fse-gtw-eds-client/src/test/resources/certs/sogei/RicettaDematerializzataCryptKS.jks";
	
	private static final String certPath = "/Users/vincenzoingenito/eclipse-workspace/it-fse-gtw-eds-client/src/test/resources/certs/sogei/RicettaDematerializzataTest.cer";

    @Test
    void freeMode() {
    	CertificateManager cm = new CertificateManager();
    	
    	Map<StoreTypeEnum, PrivateKeyFormatInfoDto> privateKeyFormats = new HashMap<>();
    	PrivateKeyFormatInfoDto key = PrivateKeyFormatInfoDto.builder().
    			storageType(StoreTypeEnum.JKS).
    			alias("ricettadematerializzatatest").
    			pwd("pippo".toCharArray()).privKeyPath(privKeyPath).build();

		privateKeyFormats.put(StoreTypeEnum.JKS, key);
    	
		cm.addCertificateToVault(Arrays.asList("dema"),
				certPath,
    			privateKeyFormats, 
    			TransformationEnum.RSA);
		
		String encoded = cm.encodeCms("DC1e6TLRSexjCI0bUkhpS9LzoLu59+hd9JyY/+3Y65M=", "Ciao Mondo!");
		String decoded = cm.decodeCms(StoreTypeEnum.JKS, encoded);
		System.out.println(decoded);
    }
 

}