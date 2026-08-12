package it.finanze.sanita.fse2.ms.edsclient.client;


import it.finanze.sanita.fse2.ms.edsclient.dto.EdsResponseDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.BrokerRequestDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.response.GetDocumentReferenceResDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.response.GetIngestionStatusResponseDTO;

/**
 * Interface of Eds client.
 */
public interface IBrokerClient {

	EdsResponseDTO dispatchAndSendData(BrokerRequestDTO brokerRequestDTO);
	
	GetDocumentReferenceResDTO getDocumentReference(String fiscalCode, String masterIdentifier, String jwt);

    GetIngestionStatusResponseDTO getIngestionStatus(String workflowInstanceId);
}