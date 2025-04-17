package it.finanze.sanita.fse2.ms.edsclient.nce.utility;

import java.io.File;
import java.nio.file.Files;

import it.finanze.sanita.fse2.ms.edsclient.nce.exception.CipherException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtility {

	public static byte[] loadContentFromLocation(String inX509PubCertPath) {
		return readFileAsBytes(inX509PubCertPath);
	}
    
	private static byte[] readFileAsBytes(String path) {
    	try {
            File file = new File(path);
            return Files.readAllBytes(file.toPath());
    	} catch (Exception e) {
    		log.error("Read file error.", e);
    		throw new CipherException(e);
    	}
    }
    
}
