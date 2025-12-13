package it.finanze.sanita.fse2.ms.edsclient.client;


import it.finanze.sanita.fse2.ms.edsclient.dto.EdsResponseDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.BrokerRequestDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.response.GetDocumentReferenceResDTO;

/**
 * Interface of Eds client.
 */
public interface IBrokerClient {

	EdsResponseDTO dispatchAndSendData(BrokerRequestDTO brokerRequestDTO);
	
	GetDocumentReferenceResDTO getDocumentReference(String fiscalCode, String masterIdentifier);
}

