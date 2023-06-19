/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package it.finanze.sanita.fse2.ms.edsclient.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import it.finanze.sanita.fse2.ms.edsclient.dto.AbstractDTO;
import it.finanze.sanita.fse2.ms.edsclient.enums.AttivitaClinicaEnum;
import it.finanze.sanita.fse2.ms.edsclient.enums.HealthcareFacilityEnum;
import it.finanze.sanita.fse2.ms.edsclient.enums.PracticeSettingCodeEnum;
import it.finanze.sanita.fse2.ms.edsclient.enums.TipoDocAltoLivEnum;
import lombok.*;

import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PublicationMetadataReqDTO extends AbstractDTO {

	private static final long serialVersionUID = 8736849367644751300L;

	@Schema(description = "Tipologia struttura che ha prodotto il documento", required = true)
	private HealthcareFacilityEnum tipologiaStruttura;

	@Schema(description = "Regole di accesso")
	@Size(min = 0, max = 100)
	private List<String> attiCliniciRegoleAccesso;

	@Schema(description = "Tipo documento alto livello", required = true)
	private TipoDocAltoLivEnum tipoDocumentoLivAlto;

	@Schema(description = "Assetto organizzativo che ha portato alla creazione del documento", required = true)
	private PracticeSettingCodeEnum assettoOrganizzativo;
	 
	@Schema(description = "Data inizio prestazione")
	@Size(min = 0, max = 100)
	private String dataInizioPrestazione;

	@Schema(description = "Data fine prestazione")
	@Size(min = 0, max = 100)
	private String dataFinePrestazione;

	@Schema(description = "Conservazione a norma")
	@Size(min = 0, max = 100)
	private String conservazioneANorma;

	@Schema(description = "Tipo attività clinica",required = true)
	private AttivitaClinicaEnum tipoAttivitaClinica;

	@Schema(description = "Identificativo sottomissione",required = true)
	@Size(min = 0, max = 100)
	private String identificativoSottomissione;

}
