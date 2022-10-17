package it.finanze.sanita.fse2.ms.edsclient.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class DocumentResponseDTO extends ResponseDTO { 

	public DocumentResponseDTO(final LogTraceInfoDTO traceInfo) {
		super(traceInfo);
	}
	
}
