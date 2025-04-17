package it.finanze.sanita.fse2.ms.edsclient.nce.utility;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Map.Entry;

import javax.crypto.Cipher;

import org.bouncycastle.asn1.x500.X500Name;

import it.finanze.sanita.fse2.ms.edsclient.nce.core.CertificateVault;
import it.finanze.sanita.fse2.ms.edsclient.nce.dto.KeyPairDto;
import it.finanze.sanita.fse2.ms.edsclient.nce.enums.StoreTypeEnum;
import it.finanze.sanita.fse2.ms.edsclient.nce.enums.TransformationEnum;
import it.finanze.sanita.fse2.ms.edsclient.nce.exception.CipherException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CertificateCipherUtility {


    private static String encodeWithCipher(Certificate pubKeyHolder, TransformationEnum trasformation, String plaintext) {
    	try {
            PublicKey publicKey = pubKeyHolder.getPublicKey();
            Cipher cipherEncryptMode = Cipher.getInstance(trasformation.getTrasformation(), "BC");
            cipherEncryptMode.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);
            byte[] encryptedBytes = cipherEncryptMode.doFinal(plaintextBytes);
            Encoder encoder = Base64.getEncoder();
            return encoder.encodeToString(encryptedBytes);
    	} catch (Exception e) {
    		log.error("Encoding error.", e);
    		throw new CipherException(e);
    	}
    }

    public static String encodeWithCipher(CertificateVault vault, String kid, TransformationEnum trasformation, String plaintext) {
    	KeyPairDto kp = vault.getKeyPair(kid);
    	TransformationEnum realTrasformation = getTrasformation(trasformation, kp);
        return encodeWithCipher(kp.getPubKeyHolder(), realTrasformation, plaintext);
    }

	private static TransformationEnum getTrasformation(TransformationEnum trasformation, KeyPairDto kp) {
		TransformationEnum realTrasformation = trasformation;
    	if (realTrasformation == null) {
    		realTrasformation = kp.getDefautlTrasformation();
    	}
		if (realTrasformation==null) {
			throw new CipherException("Trasformation must be present.");
		}
		return realTrasformation;
	}

    private static String decodeWithCipher(PrivateKey privateKey, TransformationEnum trasformation, String ciphertext) {
    	try {
	        Cipher cipherDecryptMode = Cipher.getInstance(trasformation.getTrasformation());
	        cipherDecryptMode.init(Cipher.DECRYPT_MODE, privateKey);
	        byte[] encryptedBytes = Base64.getDecoder().decode(ciphertext);
	        byte[] decryptedBytes = cipherDecryptMode.doFinal(encryptedBytes);
	        return new String(decryptedBytes, StandardCharsets.UTF_8);
		} catch (Exception e) {
			log.error("Decoding error.", e);
    		throw new CipherException(e);
		}
    }

    public static String decodeWithCipher(CertificateVault vault, String kid, StoreTypeEnum ste, TransformationEnum trasformation, String ciphertext) {
    	KeyPairDto kp = vault.getKeyPair(kid);
    	TransformationEnum realTrasformation = getTrasformation(trasformation, kp);
    	PrivateKey pk = kp.getPrivateKeyFormats().values().iterator().next();
    	if (ste != null) {
    		pk = kp.getPrivateKeyFormats().get(ste);
    	}
    	return decodeWithCipher(pk, realTrasformation, ciphertext);
    }

	private static String getKidFromIssuerAndSerial(CertificateVault vault, String issuer, String sNumber) {
		KeyPairDto kpd = null;
		for (Entry<String, KeyPairDto> kp:vault.entrySet()) {
			if (new X500Name(kp.getValue().getIssuer()).equals(new X500Name(issuer)) && kp.getValue().getSerialNumber().equalsIgnoreCase(sNumber)) {
				kpd = kp.getValue();
				break;
			}
		}
		return kpd.getKid();
	}
	
    public static String encodeWithCipher(CertificateVault vault, String issuer, String sNumber, TransformationEnum trasformation, String plaintext) {
    	String kid = getKidFromIssuerAndSerial(vault, issuer, sNumber);
	    return CertificateCipherUtility.encodeWithCipher(vault, kid, trasformation, plaintext);
    }

    public static String decodeWithCipher(CertificateVault vault, String issuer, String sNumber, StoreTypeEnum ste, TransformationEnum trasformation, String ciphertext) {
    	String kid = getKidFromIssuerAndSerial(vault, issuer, sNumber);
		return CertificateCipherUtility.decodeWithCipher(vault, kid, ste, trasformation, ciphertext);
    }

}