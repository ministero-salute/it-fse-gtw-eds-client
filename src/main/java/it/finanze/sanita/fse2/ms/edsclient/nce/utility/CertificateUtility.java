package it.finanze.sanita.fse2.ms.edsclient.nce.utility;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Enumeration;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import it.finanze.sanita.fse2.ms.edsclient.nce.enums.StoreTypeEnum;
import it.finanze.sanita.fse2.ms.edsclient.nce.exception.CipherException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CertificateUtility {

    public static void enableBouncyCastleProvider() {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static PrivateKey loadPrivateKey(StoreTypeEnum ste, byte[] keystoreBytes, String alias, char[] pwd) {
	    try {
	    	InputStream keyStoreStream = new ByteArrayInputStream(keystoreBytes);
	    	KeyStore keyStore = KeyStore.getInstance(ste.getType());
	        keyStore.load(keyStoreStream, pwd);
	        // Ottieni la chiave privata dal keystore
	        return (PrivateKey) keyStore.getKey(alias, pwd);
	    } catch (Exception e) {
			log.error("Loading private key error.", e);
			throw new CipherException(e);
	    }
	}
    
    public static X509Certificate loadPubKeyHolder(byte[] certificateBytes) {
        try {
        	InputStream is = new ByteArrayInputStream(certificateBytes);
        	CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) certificateFactory.generateCertificate(is);
        } catch (Exception e) {
			log.error("Loading certificate error.", e);
    		throw new CipherException(e);
        }
    }

    public static String getKid(Certificate pubKeyHolder) {
        try {
            // Ottieni la chiave pubblica e calcola l'hash
            byte[] publicKeyBytes = pubKeyHolder.getPublicKey().getEncoded();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(publicKeyBytes);
            // Converte in Base64
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new CipherException("Errore durante la generazione del Key ID", e);
        }
    }
    
    public static Integer getKeyLength(X509Certificate certificate) {
        Integer out = null;
        try {
            PublicKey publicKey = certificate.getPublicKey();
            if (publicKey instanceof RSAPublicKey) {
                out = ((RSAPublicKey) publicKey).getModulus().bitLength();
            } else {
                log.error("The certificate does not contain an RSA public key.");
            }
        } catch (Exception e) {
            log.error("Error getting RSA key length.", e);
        }
        return out;
    }

	public static void fromP12toJKS(String p12FilePath, String jksFilePath, char[] password) {
    	try {
            // Carica il keystore PKCS#12
            KeyStore p12KeyStore = KeyStore.getInstance("PKCS12");
            FileInputStream p12File = new FileInputStream(p12FilePath);
            p12KeyStore.load(p12File, password);
            
            // Crea un nuovo keystore JKS
            KeyStore jksKeyStore = KeyStore.getInstance("JKS");
            jksKeyStore.load(null, null);  // Inizializza un keystore vuoto
            
            // Copia le voci dal PKCS#12 al JKS
            Enumeration<String> aliases = p12KeyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                if (p12KeyStore.isKeyEntry(alias)) {
                    Key key = p12KeyStore.getKey(alias, password);
                     Certificate[] certChain = p12KeyStore.getCertificateChain(alias);
                    jksKeyStore.setKeyEntry(alias, key, password, certChain);
                }
            }
            
            // Salva il JKS su disco
            FileOutputStream jksFile = new FileOutputStream(jksFilePath);
            jksKeyStore.store(jksFile, password);    		
		} catch (Exception e) {
			log.error("Error converting P12 into JKS", e);
    		throw new CipherException(e);
		}
	}
	
	public static void fromJKStoP12(String jksFilePath, String p12FilePath, char[] password) {
	    try {
	        // Carica il keystore JKS
	        KeyStore jksKeyStore = KeyStore.getInstance("JKS");
	        try (FileInputStream jksFile = new FileInputStream(jksFilePath)) {
	            jksKeyStore.load(jksFile, password);
	        }

	        // Crea un nuovo keystore PKCS#12
	        KeyStore p12KeyStore = KeyStore.getInstance("PKCS12");
	        p12KeyStore.load(null, null); // Inizializza un keystore vuoto

	        // Copia le voci dal JKS al PKCS#12
	        Enumeration<String> aliases = jksKeyStore.aliases();
	        while (aliases.hasMoreElements()) {
	            String alias = aliases.nextElement();
	            if (jksKeyStore.isKeyEntry(alias)) {
	                Key key = jksKeyStore.getKey(alias, password);
	                Certificate[] certChain = jksKeyStore.getCertificateChain(alias);
	                p12KeyStore.setKeyEntry(alias, key, password, certChain);
	            }
	        }

	        // Salva il PKCS#12 su disco
	        try (FileOutputStream p12File = new FileOutputStream(p12FilePath)) {
	            p12KeyStore.store(p12File, password);
	        }
	    } catch (Exception e) {
	        log.error("Error converting JKS into P12", e);
	        throw new CipherException(e);
	    }
	}

}
