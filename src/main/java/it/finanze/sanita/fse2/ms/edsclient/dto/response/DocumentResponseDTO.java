package it.finanze.sanita.fse2.ms.edsclient.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class DocumentResponseDTO extends ResponseDTO { 

	public DocumentResponseDTO(final LogTraceInfoDTO traceInfo) {
		super(traceInfo);
	}
	
}
