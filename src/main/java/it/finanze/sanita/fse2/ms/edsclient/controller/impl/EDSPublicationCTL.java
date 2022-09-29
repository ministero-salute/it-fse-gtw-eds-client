package it.finanze.sanita.fse2.ms.edsclient.controller.impl;

import it.finanze.sanita.fse2.ms.edsclient.dto.request.EdsMetadataUpdateReqDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.PublicationRequestBodyDTO;
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
    public EDSPublicationResponseDTO publication(final PublicationRequestBodyDTO requestBodyDTO, HttpServletRequest request) {
        log.info("Workflow instance id received:" + requestBodyDTO.getWorkflowInstanceId() +", calling eds invocation client...");
        Boolean res = edsInvocationSRV.publishByWorkflowInstanceIdAndPriority(requestBodyDTO);
        return new EDSPublicationResponseDTO(getLogTraceInfo(), res);
    }

	@Override
	public EDSPublicationResponseDTO delete(String ooid, HttpServletRequest request) {
		log.info("Ricevuto ooid : " + ooid );
		Boolean res = edsInvocationSRV.deleteByIdentifier(ooid);
		return new EDSPublicationResponseDTO(getLogTraceInfo(), res);
	}

	@Override
	public EDSPublicationResponseDTO replace(final IndexerValueDTO replaceInfo, final HttpServletRequest request) {
		log.info("Executing replace operation of document having identifier: {}", replaceInfo.getIdentificativoDocUpdate());
		Boolean res = edsInvocationSRV.replaceByWorkflowInstanceIdAndIdentifier(replaceInfo.getIdentificativoDocUpdate(), replaceInfo.getWorkflowInstanceId());
		return new EDSPublicationResponseDTO(getLogTraceInfo(), res);
	}

	@Override
	public EDSPublicationResponseDTO update(EdsMetadataUpdateReqDTO dto, HttpServletRequest request) {
		log.info("Executing update operation of document having identifier: {}", dto.getIdDoc());
		Boolean res = edsInvocationSRV.updateByRequest(dto);
		return new EDSPublicationResponseDTO(getLogTraceInfo(), res);
	}

}
