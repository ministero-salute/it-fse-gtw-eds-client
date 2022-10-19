/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.edsclient.service;

import it.finanze.sanita.fse2.ms.edsclient.dto.EdsResponseDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.EdsMetadataUpdateReqDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.PublicationRequestBodyDTO;

public interface IEdsInvocationSRV {

	EdsResponseDTO publishByWorkflowInstanceIdAndPriority(PublicationRequestBodyDTO requestBodyDTO);
	
	EdsResponseDTO deleteByIdentifier(String identifier);
	
	EdsResponseDTO replaceByWorkflowInstanceIdAndIdentifier(String identifier, String workflowInstanceId);
	
	EdsResponseDTO updateByRequest(String idDoc, EdsMetadataUpdateReqDTO updateReqDTO);
}
