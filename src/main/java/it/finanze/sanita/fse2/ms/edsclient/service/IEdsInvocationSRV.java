package it.finanze.sanita.fse2.ms.edsclient.service;

import it.finanze.sanita.fse2.ms.edsclient.dto.request.EdsMetadataUpdateReqDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.PublicationRequestBodyDTO;

public interface IEdsInvocationSRV {

	Boolean publishByWorkflowInstanceIdAndPriority(PublicationRequestBodyDTO requestBodyDTO);
	
	Boolean deleteByIdentifier(String identifier);
	
	Boolean replaceByWorkflowInstanceIdAndIdentifier(String identifier, String workflowInstanceId);
	
	Boolean updateByRequest(String idDoc, EdsMetadataUpdateReqDTO updateReqDTO);
}
