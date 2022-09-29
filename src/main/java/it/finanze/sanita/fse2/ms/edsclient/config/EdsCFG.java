package it.finanze.sanita.fse2.ms.edsclient.config;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class EdsCFG implements Serializable {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 7517807186612242808L;

	/** EDS Ingestion Config **/
	@Value("${eds-ingestion.url.host}")
	private String edsIngestionHost;


}
