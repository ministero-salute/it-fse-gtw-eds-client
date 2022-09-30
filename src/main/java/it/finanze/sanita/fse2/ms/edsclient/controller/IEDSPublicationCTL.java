package it.finanze.sanita.fse2.ms.edsclient.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Size;

import it.finanze.sanita.fse2.ms.edsclient.dto.request.EdsMetadataUpdateReqDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.PublicationRequestBodyDTO;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.IndexerValueDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.response.EDSPublicationResponseDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.response.ErrorResponseDTO;


/**
 *
 *	Controller EDS publication.
 *	@author Riccardo Bonesi
 */
@RequestMapping(path = "/v1")
@Tag(name = "Servizio pubblicazione verso INI")
public interface IEDSPublicationCTL {

    @PostMapping("/documents")
	@Operation(summary = "Pubblicazione risorsa FHIR ad EDS", description = "Invio di una risorsa FHIR ad EDS.")
	@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EDSPublicationResponseDTO.class)))
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Pubblicazione eseguita con successo", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EDSPublicationResponseDTO.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))) })
    EDSPublicationResponseDTO publication(@RequestBody PublicationRequestBodyDTO requestBodyDTO, HttpServletRequest request);
    
	@PutMapping("/documents/{idDoc}")
	@Operation(summary = "Sostituzione risorsa fhir", description = "Sostituzione risorsa fhir.")
	@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class)))
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Sostituzione risorsa eseguita con successo", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EDSPublicationResponseDTO.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))) })
	EDSPublicationResponseDTO replace(@Size(min = 1, max = 256) @PathVariable(value = "idDoc" , required = true) String idDoc, @RequestBody IndexerValueDTO replaceInfo, HttpServletRequest request);

	@PutMapping("/documents/{idDoc}/metadata")
	@Operation(summary = "Aggiornamento risorsa fhir", description = "Aggiornamento risorsa fhir.")
	@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EDSPublicationResponseDTO.class)))
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Aggiornamento risorsa eseguito con successo", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EDSPublicationResponseDTO.class))),
		@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
		@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))) })
	EDSPublicationResponseDTO update(@Size(min = 1, max = 256) @PathVariable(value = "idDoc" , required = true) String idDoc, @RequestBody EdsMetadataUpdateReqDTO req, HttpServletRequest request);

    @DeleteMapping("/documents/{idDoc}")
	@Operation(summary = "Delete risorsa fhir", description = "Delete risorsa fhir.")
	@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class)))
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Eliminazione eseguita con successo", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EDSPublicationResponseDTO.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))) })
    EDSPublicationResponseDTO delete(@Size(min = 1, max = 256) @PathVariable(value = "idDoc", required = true) String idDoc, HttpServletRequest request);

    
}
