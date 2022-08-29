package it.finanze.sanita.fse2.ms.edsclient.controller.impl;

import it.finanze.sanita.fse2.ms.edsclient.dto.request.EdsMetadataUpdateReqDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import it.finanze.sanita.fse2.ms.edsclient.controller.IEDSPublicationCTL;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.IndexerValueDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.response.EDSPublicationResponseDTO;
import it.finanze.sanita.fse2.ms.edsclient.service.IEdsInvocationSRV;

import javax.servlet.http.HttpServletRequest;

/**
 *
 *	INI Publication controller.
 */
@Slf4j
@RestController
public class EDSPublicationCTL extends AbstractCTL implements IEDSPublicationCTL {

    /**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 8414558048109050743L;
	
	@Autowired
	private IEdsInvocationSRV edsInvocationSRV;
    
    @Override
    public EDSPublicationResponseDTO publication(final String workflowInstanceId, HttpServletRequest request) {
        log.info("Workflow instance id received:" + workflowInstanceId +", calling eds invocation client...");
        Boolean res = edsInvocationSRV.findAndSendToEdsByWorkflowInstanceId(workflowInstanceId);
        return new EDSPublicationResponseDTO(getLogTraceInfo(), res);
    }

	@Override
	public Boolean delete(String ooid, HttpServletRequest request) {
		log.info("Ricevuto ooid : " + ooid );
		
		// TODO: Implementare chiamata ad eds
		return true;
	}

	@Override
	public EDSPublicationResponseDTO replace(final IndexerValueDTO replaceInfo, final HttpServletRequest request) {
		log.info("Executing replace operation of document having identifier: {}", replaceInfo.getIdentificativoDocUpdate());
		
		// TODO: Implementare chiamata ad eds
		log.info("Mocking replace - EDS is not yet ready");
		return new EDSPublicationResponseDTO(getLogTraceInfo(), true);
	}

	@Override
	public EDSPublicationResponseDTO update(EdsMetadataUpdateReqDTO dto, HttpServletRequest request) {
		return new EDSPublicationResponseDTO(getLogTraceInfo(), true);
	}

}
