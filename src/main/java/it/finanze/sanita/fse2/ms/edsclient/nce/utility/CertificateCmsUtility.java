//package it.finanze.sanita.fse2.ms.edsclient.nce.utility;
//
//import java.nio.charset.StandardCharsets;
//import java.security.PrivateKey;
//import java.security.cert.Certificate;
//import java.security.cert.X509Certificate;
//import java.util.Base64;
//import java.util.Base64.Encoder;
//import java.util.Collection;
//import java.util.Map.Entry;
//
//import org.bouncycastle.asn1.x500.X500Name;
//import org.bouncycastle.cms.CMSAlgorithm;
//import org.bouncycastle.cms.CMSEnvelopedData;
//import org.bouncycastle.cms.CMSEnvelopedDataGenerator;
//import org.bouncycastle.cms.CMSEnvelopedDataParser;
//import org.bouncycastle.cms.CMSProcessableByteArray;
//import org.bouncycastle.cms.KeyTransRecipientId;
//import org.bouncycastle.cms.RecipientInformation;
//import org.bouncycastle.cms.RecipientInformationStore;
//import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
//import org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
//import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
//import org.bouncycastle.operator.OutputEncryptor;
//
//import it.finanze.sanita.fse2.ms.edsclient.nce.core.CertificateVault;
//import it.finanze.sanita.fse2.ms.edsclient.nce.dto.KeyPairDto;
//import it.finanze.sanita.fse2.ms.edsclient.nce.enums.StoreTypeEnum;
//import it.finanze.sanita.fse2.ms.edsclient.nce.exception.CipherException;
//import lombok.AccessLevel;
//import lombok.NoArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//@NoArgsConstructor(access = AccessLevel.PRIVATE)
//public class CertificateCmsUtility {
//
//    private static String encodeWithCms(Certificate certificate, String plaintext) {
//    	try {
//    	    byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);
//    		
//    	    // Cifratura con la chiave pubblica
//    	    CMSEnvelopedDataGenerator envelopedDataGen = new CMSEnvelopedDataGenerator();
//    	    
//    	    // Aggiungi il destinatario utilizzando la chiave pubblica del certificato
//    	    envelopedDataGen.addRecipientInfoGenerator(new JceKeyTransRecipientInfoGenerator((X509Certificate)certificate).setProvider("BC"));
//    	    
//    	    // Crea un oggetto CMSProcessable per i dati da cifrare
//    	    CMSProcessableByteArray cmsData = new CMSProcessableByteArray(plaintextBytes);
//    	    
//    	    // Costruisci l'output encryptor per AES256-CBC
//    	    OutputEncryptor encryptor = new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES256_CBC).setProvider("BC").build();
//    	    
//    	    // Genera i dati cifrati
//    	    CMSEnvelopedData envelopedData = envelopedDataGen.generate(cmsData, encryptor);
//    	    
//    	    // Restituisci i dati cifrati
//    	    Encoder encoder = Base64.getEncoder();
//    	    return encoder.encodeToString(envelopedData.getEncoded());
//		} catch (Exception e) {
//			log.error("Encoding CMS error.", e);
//    		throw new CipherException(e);
//		}
//	}
//	
//	public static String encodeWithCms(CertificateVault vault, String kid, String plaintext) {
//		KeyPairDto kp = vault.getKeyPair(kid);
//		return encodeWithCms(kp.getPubKeyHolder(), plaintext);
//	}
//
//	public static String encodeWithCms(CertificateVault vault, String issuer, String sNumber, String plaintext) {
//		KeyPairDto kpd = null;
//		for (Entry<String, KeyPairDto> kp:vault.entrySet()) {
//			if (new X500Name(kp.getValue().getIssuer()).equals(new X500Name(issuer)) && kp.getValue().getSerialNumber().equalsIgnoreCase(sNumber)) {
//				kpd = kp.getValue();
//				break;
//			}
//		}
//		return encodeWithCms(kpd.getPubKeyHolder(), plaintext);
//	}
//
//    public static String decodeWithCms(CertificateVault vault, String kid, StoreTypeEnum ste, String ciphertext) {
//    	KeyPairDto kp = vault.getKeyPair(kid);
//    	PrivateKey pk = kp.getPrivateKeyFormats().values().iterator().next();
//    	if (ste != null) {
//    		pk = kp.getPrivateKeyFormats().get(ste);
//    	}
//    	try {
//            byte[] encryptedData = Base64.getDecoder().decode(ciphertext);
//
//            // Crea un parser per i dati cifrati
//            CMSEnvelopedDataParser envelopedDataParser = new CMSEnvelopedDataParser(encryptedData);
//            
//            RecipientInformation ei =  (RecipientInformation) envelopedDataParser.getRecipientInfos().getRecipients().iterator().next();
////            KeyTransRecipientId k = (KeyTransRecipientId) ei.getRID();
//            return decodeWithCms(pk, envelopedDataParser);
//		} catch (Exception e) {
//			log.error("Decoding CMS error.", e);
//    		throw new CipherException(e);
//		}
//    }
//    
//	private static String decodeWithCms(PrivateKey privateKey, CMSEnvelopedDataParser envelopedDataParser) {
//    	try {
//    		// Estrai tutte le RecipientInformation (informazioni sui destinatari)
//            RecipientInformationStore recipients = envelopedDataParser.getRecipientInfos();
//            
//            // Trova il destinatario (recipient) appropriato che corrisponde alla chiave privata
//            Collection<RecipientInformation> recipientCollection = recipients.getRecipients();
//            RecipientInformation recipientInfo = recipientCollection.iterator().next(); // Presupponendo un solo destinatario
//
//            // Decodifica il contenuto utilizzando la chiave privata
//            byte[] decryptedData = recipientInfo.getContent(new JceKeyTransEnvelopedRecipient(privateKey).setProvider("BC"));
//            
//            return new String(decryptedData, StandardCharsets.UTF_8);
//		} catch (Exception e) {
//			log.error("Decoding CMS error.", e);
//    		throw new CipherException(e);
//		}
//	}
//
//	public static String decodeWithCms(CertificateVault vault, String ciphertext, StoreTypeEnum ste) {
//    	try {
//        	byte[] encryptedData = Base64.getDecoder().decode(ciphertext);
//
//            // Crea un parser per i dati cifrati
//            CMSEnvelopedDataParser envelopedDataParser = new CMSEnvelopedDataParser(encryptedData);
//            
//            RecipientInformation ei =  (RecipientInformation) envelopedDataParser.getRecipientInfos().getRecipients().iterator().next();
//            KeyTransRecipientId k = (KeyTransRecipientId) ei.getRID();
//            String issuer = k.getIssuer().toString();
//            String sNumber = k.getSerialNumber().toString();
//            
//        	KeyPairDto kpd = null;
//    		for (Entry<String, KeyPairDto> kp:vault.entrySet()) {
//    			if (kp.getValue().getIssuer() == null || kp.getValue().getSerialNumber() == null) {
//    				continue;
//    			}
//    			if (new X500Name(kp.getValue().getIssuer()).equals(new X500Name(issuer)) && kp.getValue().getSerialNumber().equalsIgnoreCase(sNumber)) {
//    				kpd = kp.getValue();
//    				break;
//    			}
//    		}
//        	PrivateKey privateKey = kpd.getPrivateKeyFormats().values().iterator().next();
//        	if (ste != null) {
//        		privateKey = kpd.getPrivateKeyFormats().get(ste);
//        	}
//
//            return decodeWithCms(privateKey, envelopedDataParser);
//		} catch (Exception e) {
//			log.error("Decoding CMS error.", e);
//    		throw new CipherException(e);
//		}
//	}
//	
//	public static String decodeWithCms(CertificateVault vault, String issuer, String sNumber, String ciphertext, StoreTypeEnum ste) {
//    	try {
//        	KeyPairDto kpd = null;
//    		for (Entry<String, KeyPairDto> kp:vault.entrySet()) {
//    			if (new X500Name(kp.getValue().getIssuer()).equals(new X500Name(issuer)) && kp.getValue().getSerialNumber().equalsIgnoreCase(sNumber)) {
//    				kpd = kp.getValue();
//    				break;
//    			}
//    		}
//        	PrivateKey privateKey = kpd.getPrivateKeyFormats().values().iterator().next();
//        	if (ste != null) {
//        		privateKey = kpd.getPrivateKeyFormats().get(ste);
//        	}
//
//            byte[] encryptedData = Base64.getDecoder().decode(ciphertext);
//
//            // Crea un parser per i dati cifrati
//            CMSEnvelopedDataParser envelopedDataParser = new CMSEnvelopedDataParser(encryptedData);
//            
//            return decodeWithCms(privateKey, envelopedDataParser);
//		} catch (Exception e) {
//			log.error("Decoding CMS error.", e);
//    		throw new CipherException(e);
//		}
//	}
//	
//}
