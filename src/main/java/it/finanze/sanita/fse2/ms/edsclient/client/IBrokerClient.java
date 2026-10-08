package it.finanze.sanita.fse2.ms.edsclient.client;

import it.finanze.sanita.fse2.ms.edsclient.dto.EdsResponseDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.OptionalLogDataDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.EdsMetadataUpdateReqDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.response.GetDocumentReferenceResDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.response.GetIngestionStatusResponseDTO;
import it.finanze.sanita.fse2.ms.edsclient.repository.entity.IniEdsInvocationETY;

/**
 * Client verso il broker EDS. Espone un metodo dedicato per ciascuna operazione
 * invece di un unico dispatcher generico, in modo che il contratto sia esplicito
 * e ogni chiamata trasporti esattamente i parametri necessari.
 */
public interface IBrokerClient {

	/**
	 * Pubblica un documento nuovo (POST).
	 *
	 * @param idDoc              identificativo documento
	 * @param workflowInstanceId workflow instance id
	 * @param ety                entità con dati e metadati da Mongo
	 * @param logData            dati opzionali per il log strutturato
	 */
	EdsResponseDTO publish(String idDoc, String workflowInstanceId, IniEdsInvocationETY ety, OptionalLogDataDTO logData);

	/**
	 * Sostituisce un documento esistente (PUT).
	 *
	 * @param idDoc              identificativo documento
	 * @param workflowInstanceId workflow instance id
	 * @param ety                entità con dati e metadati da Mongo
	 * @param logData            dati opzionali per il log strutturato
	 */
	EdsResponseDTO replace(String idDoc, String workflowInstanceId, IniEdsInvocationETY ety, OptionalLogDataDTO logData);

	/**
	 * Elimina un documento (DELETE).
	 *
	 * @param identifier identificativo documento
	 * @param fiscalCode codice fiscale del paziente
	 * @param jwt        token JWT in ingresso dalla request
	 */
	EdsResponseDTO delete(String identifier, String fiscalCode, String jwt);

	/**
	 * Aggiorna i metadati di un documento (PUT /metadata).
	 *
	 * @param idDoc         identificativo documento
	 * @param updateReqDTO  payload con il document reference aggiornato
	 * @param fiscalCode    codice fiscale del paziente
	 * @param jwt           token JWT in ingresso dalla request
	 */
	EdsResponseDTO update(String idDoc, EdsMetadataUpdateReqDTO updateReqDTO, String fiscalCode, String jwt);

	GetDocumentReferenceResDTO getDocumentReference(String fiscalCode, String masterIdentifier, String jwt);

	GetIngestionStatusResponseDTO getIngestionStatus(String workflowInstanceId);
}
