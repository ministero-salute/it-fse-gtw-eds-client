package it.finanze.sanita.fse2.ms.edsclient.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.edsclient.client.IEdsClient;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.EdsMetadataUpdateReqDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.IngestorRequestDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.PublicationRequestBodyDTO;
import it.finanze.sanita.fse2.ms.edsclient.enums.ProcessorOperationEnum;
import it.finanze.sanita.fse2.ms.edsclient.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.edsclient.repository.IEdsInvocationRepo;
import it.finanze.sanita.fse2.ms.edsclient.repository.entity.IniEdsInvocationETY;
import it.finanze.sanita.fse2.ms.edsclient.service.IEdsInvocationSRV;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EdsInvocationSRV implements IEdsInvocationSRV {

	@Autowired
	private IEdsInvocationRepo edsInvocationRepo;

	@Autowired
	private IEdsClient edsClient;

	 
	
	@Override
	public Boolean publishByWorkflowInstanceIdAndPriority(final PublicationRequestBodyDTO requestBodyDTO) {
		boolean out = false;
		try {
			IniEdsInvocationETY iniEdsInvocationETY = edsInvocationRepo
					.findByWorkflowInstanceId(requestBodyDTO.getWorkflowInstanceId());
			if (iniEdsInvocationETY != null && iniEdsInvocationETY.getData() != null) {
				out = edsClient.dispatchAndSendData(
						IngestorRequestDTO.builder()
								.updateReqDTO(null)
								.iniEdsInvocationETY(iniEdsInvocationETY)
								.operation(ProcessorOperationEnum.PUBLISH)
								.identifier(requestBodyDTO.getIdentificativoDoc())
								.priorityType(requestBodyDTO.getPriorityType())
								.build()
				);
			}
		} catch (Exception ex) {
			log.error("Error while running find and send to eds by workflow instance id : ", ex);
			throw new BusinessException(ex);
		}
		return out;
	}

	@Override
	public Boolean deleteByIdentifier(final String identifier) {
		boolean out = false;
		try {
			out = edsClient.dispatchAndSendData(
					IngestorRequestDTO.builder()
							.updateReqDTO(null)
							.iniEdsInvocationETY(null)
							.identifier(identifier)
							.operation(ProcessorOperationEnum.DELETE)
							.priorityType(null)
							.build());
		} catch (Exception ex) {
			log.error("Error while running delete by identifier : ", ex);
			throw new BusinessException(ex);
		}
		return out;
	}

	@Override
	public Boolean replaceByWorkflowInstanceIdAndIdentifier(String identifier, String workflowInstanceId) {
		boolean out = false;
		IniEdsInvocationETY iniEdsInvocationETY = edsInvocationRepo.findByWorkflowInstanceId(workflowInstanceId);
		if (iniEdsInvocationETY != null && iniEdsInvocationETY.getData() != null) {
			out = edsClient.dispatchAndSendData(
					IngestorRequestDTO.builder()
							.updateReqDTO(null)
							.iniEdsInvocationETY(iniEdsInvocationETY)
							.operation(ProcessorOperationEnum.REPLACE)
							.identifier(identifier)
							.priorityType(null)
							.build()
			);
		}
		return out;
	}

	@Override
	public Boolean updateByRequest(EdsMetadataUpdateReqDTO updateReqDTO) {
		boolean out = false;
		out = edsClient.dispatchAndSendData(
				IngestorRequestDTO.builder()
						.updateReqDTO(updateReqDTO)
						.iniEdsInvocationETY(null)
						.operation(ProcessorOperationEnum.UPDATE)
						.identifier(updateReqDTO.getIdDoc())
						.priorityType(null)
						.build()
		);
		return out;
	}
}
