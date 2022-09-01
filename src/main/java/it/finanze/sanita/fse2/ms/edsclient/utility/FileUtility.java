package it.finanze.sanita.fse2.ms.edsclient.utility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import it.finanze.sanita.fse2.ms.edsclient.exceptions.BusinessException;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class FileUtils.
 *
 * @author vincenzoingenito
 *
 * Utility to manager file.
 */
@Slf4j
public final class FileUtility {
 
	/**
	 * Max size chunk.
	 */
	private static final int CHUNK_SIZE = 16384;

	/**
	 * Constructor.
	 */
	private FileUtility() {
	}

	public static ByteArrayInputStream getFileFromExternalResources(final String path) {
		InputStream is = null;
		try {
			if (path.contains("classpath:")) {
				String sanitizedPath = path.replace("classpath:", "");
				is = Thread.currentThread().getContextClassLoader().getResourceAsStream(sanitizedPath);
			} else {
				is = Files.newInputStream(Paths.get(path));
			}
			return new ByteArrayInputStream(FileUtility.getByteFromInputStream(is));
		} catch (Exception e) {
			throw new BusinessException("Errore in fase di recupero contenuto del file:" + e.getMessage());
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					log.error("Error while retrieving file from file system");
				}
			}
		}
	}

	/**
	 * Recupero contenuto file da input stream.
	 *
	 * @param is
	 *            input stream
	 * @return contenuto file
	 */
	private static byte[] getByteFromInputStream(final InputStream is) {
		byte[] b;
		try {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			int nRead;
			byte[] data = new byte[CHUNK_SIZE];

			while ((nRead = is.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}
			buffer.flush();
			b = buffer.toByteArray();
		} catch (Exception e) {
			log.error("Errore durante il trasform da InputStream a byte[]: ", e);
			throw new BusinessException(e);
		}
		return b;
	}

}
