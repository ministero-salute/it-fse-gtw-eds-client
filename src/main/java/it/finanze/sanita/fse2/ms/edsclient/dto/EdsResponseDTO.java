/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.edsclient.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class EdsResponseDTO extends AbstractDTO {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 3367398009484127223L;

	private boolean esito;
	
	private String exClassCanonicalName;
	
	private String messageError;

	public EdsResponseDTO() {
		this.esito = false;
	}
}
