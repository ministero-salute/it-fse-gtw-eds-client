//package it.finanze.sanita.fse2.ms.edsclient.nce;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//import org.junit.jupiter.api.Test;
//
//import it.finanze.sanita.fse2.ms.edsclient.nce.core.CertificateManager;
//import it.finanze.sanita.fse2.ms.edsclient.nce.dto.ContentLocationDto;
//import it.finanze.sanita.fse2.ms.edsclient.nce.enums.CharsetEnum;
//import it.finanze.sanita.fse2.ms.edsclient.nce.enums.LocationTypeEnum;
//import it.finanze.sanita.fse2.ms.edsclient.nce.enums.TransformationEnum;
//
//
//class CertificateHelperTest {
//
//    @Test
//    void cipher() {
//    	
//    	CertificateManager ch = new CertificateManager(CharsetEnum.UTF_8, ContentLocationDto.builder().location("certs/sogei/manifest.json").type(LocationTypeEnum.JAR).build(), 1000);
//
//    	String plainText = "Ciao Mondo!";
//		String encryptedText1 = ch.encodeCms("DC1e6TLRSexjCI0bUkhpS9LzoLu59+hd9JyY/+3Y65M=", plainText);
//		String encryptedText2 = ch.encodeCms("CN=SistemaTSTest-Dema,O=SistemaTS,ST=Italy,C=IT", "704160114", plainText);
//		String encryptedText3 = ch.encodeCipher("DC1e6TLRSexjCI0bUkhpS9LzoLu59+hd9JyY/+3Y65M=", TransformationEnum.RSA, plainText);
//		String encryptedText4 = ch.encodeCipher("CN=SistemaTSTest-Dema,O=SistemaTS,ST=Italy,C=IT", "704160114", TransformationEnum.RSA, plainText);
//
//		String decryptedText1 = ch.decodeCms(null, encryptedText1);
//    	assertEquals(plainText, decryptedText1, "Words must be equal");
//		String decryptedText1b = ch.decodeCms("DC1e6TLRSexjCI0bUkhpS9LzoLu59+hd9JyY/+3Y65M=", null, encryptedText1);
//    	assertEquals(plainText, decryptedText1b, "Words must be equal");
//		String decryptedText2 = ch.decodeCms("CN=SistemaTSTest-Dema,O=SistemaTS,ST=Italy,C=IT", "704160114", null, encryptedText2);
//    	assertEquals(plainText, decryptedText2, "Words must be equal");
//		
//		String decryptedText3 = ch.decodeCipher("DC1e6TLRSexjCI0bUkhpS9LzoLu59+hd9JyY/+3Y65M=", null, null, encryptedText3);
//    	assertEquals(plainText, decryptedText3, "Words must be equal");
//		String decryptedText4 = ch.decodeCipher("CN=SistemaTSTest-Dema,O=SistemaTS,ST=Italy,C=IT", "704160114", null, null, encryptedText4);
//    	assertEquals(plainText, decryptedText4, "Words must be equal");
//
//    }
//
//}