//package it.finanze.sanita.fse2.ms.edsclient.nce.core;
//
//import java.security.PrivateKey;
//import java.security.cert.X509Certificate;
//import java.util.Collection;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Map.Entry;
//import java.util.Set;
//
//import it.finanze.sanita.fse2.ms.edsclient.nce.dto.KeyPairDto;
//import it.finanze.sanita.fse2.ms.edsclient.nce.dto.PrivateKeyFormatInfoDto;
//import it.finanze.sanita.fse2.ms.edsclient.nce.enums.StoreTypeEnum;
//import it.finanze.sanita.fse2.ms.edsclient.nce.enums.TransformationEnum;
//import it.finanze.sanita.fse2.ms.edsclient.nce.utility.CertificateUtility;
//import it.finanze.sanita.fse2.ms.edsclient.nce.utility.FileUtility;
//import lombok.NoArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//@NoArgsConstructor
//public class CertificateVault {
//
//	private Map<String, KeyPairDto> vault;
//
//	public Set<String> kidSet() {
//		return vault.keySet();
//	}
//
//	public KeyPairDto getKeyPair(String kid) {
//		return vault.get(kid);
//	}
//
//	public Set<Entry<String, KeyPairDto>> entrySet() {
//		return vault.entrySet();
//	}
//	
//	///////// WRITE OPERATION
//	public void add(Collection<String> inScopes, String inX509PubCertPath, Map<StoreTypeEnum, PrivateKeyFormatInfoDto> inPrivateKeyFormats, TransformationEnum inDefautlTrasformation) {
//		add(true, inScopes, inX509PubCertPath, inPrivateKeyFormats, inDefautlTrasformation);
//	}
//
//	private void add(boolean flagExclusiveWrite, Collection<String> inScopes, String inX509PubCertPath, Map<StoreTypeEnum, PrivateKeyFormatInfoDto> inPrivateKeyFormats, TransformationEnum inDefautlTrasformation) {
//		X509Certificate x509PubKeyHolder = CertificateUtility.loadPubKeyHolder(FileUtility.loadContentFromLocation(inX509PubCertPath));
//		
//		//Get info from public certificate
//		String kid = CertificateUtility.getKid(x509PubKeyHolder);
//	    Integer size = CertificateUtility.getKeyLength(x509PubKeyHolder);
//	    Date notAfter = x509PubKeyHolder.getNotAfter();
//	    String issuer = x509PubKeyHolder.getIssuerX500Principal().getName();
//	    String serialNumber = x509PubKeyHolder.getSerialNumber().toString();
//	    String keyAlg = x509PubKeyHolder.getSigAlgName();
//			
//		Map<StoreTypeEnum, PrivateKey> privateKeyFormats = new HashMap<>();
//		for (Entry<StoreTypeEnum, PrivateKeyFormatInfoDto> e:inPrivateKeyFormats.entrySet()) {
//			PrivateKey privKey = CertificateUtility.loadPrivateKey(e.getKey(), FileUtility.loadContentFromLocation(e.getValue().getPrivKeyPath()), e.getValue().getAlias(), e.getValue().getPwd());
//			privateKeyFormats.put(e.getKey(), privKey);
//		}
//		KeyPairDto dto = KeyPairDto.builder().kid(kid).pubKeyHolder(x509PubKeyHolder).keySize(size).notAfter(notAfter).scopes(inScopes).keyAlg(keyAlg).privateKeyFormats(privateKeyFormats).issuer(issuer).serialNumber(serialNumber).defautlTrasformation(inDefautlTrasformation).build();
//		
//		vault = new HashMap<>();
//		vault.put(kid, dto);
//	}
//	
//}
