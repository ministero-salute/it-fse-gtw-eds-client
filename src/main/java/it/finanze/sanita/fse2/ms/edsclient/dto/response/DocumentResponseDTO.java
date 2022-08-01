package it.finanze.sanita.fse2.ms.edsclient.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class DocumentResponseDTO extends ResponseDTO { 

	/**
	 * Serial Version UID 
	 */
	private static final long serialVersionUID = 21641554325694264L; 
	
	
	private String transactionId;

	public DocumentResponseDTO() {
		super();
	}

	public DocumentResponseDTO(final LogTraceInfoDTO traceInfo, final String inTransactionId) {
		super(traceInfo);
		transactionId = inTransactionId;
	}
	
}