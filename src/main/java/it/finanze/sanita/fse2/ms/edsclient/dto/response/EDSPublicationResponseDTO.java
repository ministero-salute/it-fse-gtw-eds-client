package it.finanze.sanita.fse2.ms.edsclient.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EDSPublicationResponseDTO extends ResponseDTO {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 5457503992983726876L;

	private Boolean esito;

	public EDSPublicationResponseDTO() {
		super();
	}

	public EDSPublicationResponseDTO(final LogTraceInfoDTO traceInfo, final Boolean inEsito) {
		super(traceInfo);
		esito = inEsito;
	}

}
