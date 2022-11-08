/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.edsclient.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class DocumentResponseDTO extends ResponseDTO { 

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -3137648330719798096L;

	public DocumentResponseDTO(final LogTraceInfoDTO traceInfo) {
		super(traceInfo);
	}
	
}
