package it.finanze.sanita.fse2.ms.edsclient.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class EdsResponseDTO extends AbstractDTO {

	private boolean esito;
	
	private String errorMessage;

	public EdsResponseDTO() {
		this.esito = false;
	}
}
