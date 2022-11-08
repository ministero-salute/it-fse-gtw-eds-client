/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.edsclient.controller.impl;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import it.finanze.sanita.fse2.ms.edsclient.controller.IEDSPublicationCTL;
import it.finanze.sanita.fse2.ms.edsclient.dto.EdsResponseDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.EdsMetadataUpdateReqDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.IndexerValueDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.PublicationRequestBodyDTO;
import it.finanze.sanita.fse2.ms.edsclient.service.IEdsInvocationSRV;
import lombok.extern.slf4j.Slf4j;

/**
 *	INI Publication controller.
 */
@Slf4j
@RestController
public class EDSPublicationCTL extends AbstractCTL implements IEDSPublicationCTL {
	
	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -358675423912784785L;
	
	@Autowired
	private transient IEdsInvocationSRV edsInvocationSRV;
    
    @Override
    public EdsResponseDTO publication(final PublicationRequestBodyDTO requestBodyDTO, HttpServletRequest request) {
        log.debug("Workflow instance id received:" + requestBodyDTO.getWorkflowInstanceId() +", calling eds invocation client...");
        return edsInvocationSRV.publishByWorkflowInstanceIdAndPriority(requestBodyDTO);
    }

	@Override
	public EdsResponseDTO delete(String ooid, HttpServletRequest request) {
		log.debug("Ricevuto ooid : " + ooid );
		return edsInvocationSRV.deleteByIdentifier(ooid);
	}

	@Override
	public EdsResponseDTO replace(final String idDoc, final IndexerValueDTO replaceInfo, final HttpServletRequest request) {
		log.debug("Executing replace operation of document having identifier: {}", replaceInfo.getIdDoc());
		return edsInvocationSRV.replaceByWorkflowInstanceIdAndIdentifier(replaceInfo.getIdDoc(), replaceInfo.getWorkflowInstanceId());
	}

	@Override
	public EdsResponseDTO update(String idDoc, EdsMetadataUpdateReqDTO dto, HttpServletRequest request) {
		log.debug("Executing update operation of document having identifier: {}", idDoc);
		return edsInvocationSRV.updateByRequest(idDoc, dto);
	}

}
