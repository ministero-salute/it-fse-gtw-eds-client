package it.finanze.sanita.fse2.ms.edsclient.nce.core;

import java.util.Collection;
import java.util.Map;

import it.finanze.sanita.fse2.ms.edsclient.nce.dto.PrivateKeyFormatInfoDto;
import it.finanze.sanita.fse2.ms.edsclient.nce.enums.ModeEnum;
import it.finanze.sanita.fse2.ms.edsclient.nce.enums.StoreTypeEnum;
import it.finanze.sanita.fse2.ms.edsclient.nce.enums.TransformationEnum;
import it.finanze.sanita.fse2.ms.edsclient.nce.exception.CipherException;
import it.finanze.sanita.fse2.ms.edsclient.nce.utility.CertificateCipherUtility;
import it.finanze.sanita.fse2.ms.edsclient.nce.utility.CertificateCmsUtility;
import it.finanze.sanita.fse2.ms.edsclient.nce.utility.CertificateUtility;
import it.finanze.sanita.fse2.ms.edsclient.nce.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CertificateManager {

	private CertificateVault vault;
	
	public CertificateManager() {
    	CertificateUtility.enableBouncyCastleProvider();
        vault = new CertificateVault();
	}
 
	
	public void addCertificateToVault(String inX509PubCertPath, Map<StoreTypeEnum, PrivateKeyFormatInfoDto> inPrivateKeyFormats) {
		vault.add(null, inX509PubCertPath, inPrivateKeyFormats, null);
	}

	public void addCertificateToVault(Collection<String> inScopes, String inX509PubCertPath, Map<StoreTypeEnum, PrivateKeyFormatInfoDto> inPrivateKeyFormats, TransformationEnum inDefautlTrasformation) {
		vault.add(inScopes, inX509PubCertPath, inPrivateKeyFormats, inDefautlTrasformation);
	}

	public String decodeCipher(String issuer, String sNumber, StoreTypeEnum ste, TransformationEnum trasformation, String ciphertext) {
		return decode(ModeEnum.CIPHER, null, issuer, sNumber, ste, trasformation, ciphertext);
	}

	public String decodeCipher(String kid, StoreTypeEnum ste, TransformationEnum trasformation, String ciphertext) {
		return decode(ModeEnum.CIPHER, kid, null, null, ste, trasformation, ciphertext);
	}

	public String decodeCms(String kid, StoreTypeEnum ste, String ciphertext) {
		return decode(ModeEnum.CMS, kid, null, null, ste, null, ciphertext);
	}

	public String decodeCms(String issuer, String sNumber, StoreTypeEnum ste, String ciphertext) {
		return decode(ModeEnum.CMS, null, issuer, sNumber, ste, null, ciphertext);
	}

	public String decodeCms(StoreTypeEnum ste, String ciphertext) {
		return decode(ModeEnum.CMS, null, null, null, ste, null, ciphertext);
	}

	public String decode(ModeEnum mode, String kid, String issuer, String sNumber, StoreTypeEnum ste, TransformationEnum trasformation, String ciphertext) {
		String out = null;
		if (ModeEnum.CIPHER.equals(mode)) {
			
			if (StringUtility.isNullOrEmpty(kid) && (StringUtility.isNullOrEmpty(issuer) || StringUtility.isNullOrEmpty(sNumber))) {
				throw new CipherException("Kid or issuer and serial number must be present.");
			}

			if (kid!=null) {
				out = CertificateCipherUtility.decodeWithCipher(vault, kid, ste, trasformation, ciphertext);
			} else {
				out = CertificateCipherUtility.decodeWithCipher(vault, issuer, sNumber, ste, trasformation, ciphertext);
			}
		} else if (ModeEnum.CMS.equals(mode)) {
			if (kid!=null) {
				out = CertificateCmsUtility.decodeWithCms(vault, kid, ste, ciphertext);
			} else if (issuer!=null && sNumber!=null) {
				out = CertificateCmsUtility.decodeWithCms(vault, issuer, sNumber, ciphertext, ste);
			} else {
				out = CertificateCmsUtility.decodeWithCms(vault, ciphertext, ste);
			}
		}
		return out;
	}

	public String encodeCms(String kid, String plaintext) {
		return encode(ModeEnum.CMS, kid, null, null, null, plaintext);
	}
	
	public String encodeCms(String issuer, String sNumber, String plaintext) {
		return encode(ModeEnum.CMS, null, issuer, sNumber, null, plaintext);
	}
	
	public String encodeCipher(String kid, TransformationEnum trasformation, String plaintext) {
		return encode(ModeEnum.CIPHER, kid, null, null, trasformation, plaintext);
	}

	public String encodeCipher(String issuer, String sNumber, TransformationEnum trasformation, String plaintext) {
		return encode(ModeEnum.CIPHER, null, issuer, sNumber, trasformation, plaintext);
	}

	public String encode(ModeEnum mode, String kid, String issuer, String sNumber, TransformationEnum trasformation, String plaintext) {
		String out = null;
		
		if (StringUtility.isNullOrEmpty(kid) && (StringUtility.isNullOrEmpty(issuer) || StringUtility.isNullOrEmpty(sNumber))) {
			throw new CipherException("Kid or issuer and serial number must be present.");
		}
		
		if (ModeEnum.CIPHER.equals(mode)) {
			if (kid!=null) {
				out = CertificateCipherUtility.encodeWithCipher(vault, kid, trasformation, plaintext);
			} else {
				out = CertificateCipherUtility.encodeWithCipher(vault, issuer, sNumber, trasformation, plaintext);
			}
		} else if (ModeEnum.CMS.equals(mode)) {
			if (kid!=null) {
				out = CertificateCmsUtility.encodeWithCms(vault, kid, plaintext);
			} else {
				out = CertificateCmsUtility.encodeWithCms(vault, issuer, sNumber, plaintext);
			}
		}
		return out;
	}
	
}
