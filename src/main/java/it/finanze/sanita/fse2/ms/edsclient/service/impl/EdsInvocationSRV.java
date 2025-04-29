/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package it.finanze.sanita.fse2.ms.edsclient.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.edsclient.client.IBrokerClient;
import it.finanze.sanita.fse2.ms.edsclient.dto.EdsResponseDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.BrokerRequestDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.EdsMetadataUpdateReqDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.PublicationRequestBodyDTO;
import it.finanze.sanita.fse2.ms.edsclient.enums.DestinationEnum;
import it.finanze.sanita.fse2.ms.edsclient.enums.ProcessorOperationEnum;
import it.finanze.sanita.fse2.ms.edsclient.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.edsclient.repository.IEdsInvocationRepo;
import it.finanze.sanita.fse2.ms.edsclient.repository.entity.IniEdsInvocationETY;
import it.finanze.sanita.fse2.ms.edsclient.service.IConfigSRV;
import it.finanze.sanita.fse2.ms.edsclient.service.IEdsInvocationSRV;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EdsInvocationSRV implements IEdsInvocationSRV {

	@Autowired
	private IEdsInvocationRepo edsInvocationRepo;

	@Autowired
	private IConfigSRV configSRV;

	@Autowired
	private IBrokerClient brokerClient;
	
	@Override
	public EdsResponseDTO publishByWorkflowInstanceId(final PublicationRequestBodyDTO requestBodyDTO) {
		EdsResponseDTO out = new EdsResponseDTO();

		IniEdsInvocationETY iniEdsInvocationETY = edsInvocationRepo.findByWorkflowInstanceId(requestBodyDTO.getWorkflowInstanceId());
		if(iniEdsInvocationETY==null || iniEdsInvocationETY.getData() == null) {
			out.setEsito(false);
			out.setMessageError("Nessun documento trovato per il workflowInstanceId: " + requestBodyDTO.getWorkflowInstanceId());
		}

		if(StringUtils.isEmpty(out.getMessageError())) {
			try {
				BrokerRequestDTO request = BrokerRequestDTO.builder().updateReqDTO(null).iniEdsInvocationETY(iniEdsInvocationETY)
						.operation(ProcessorOperationEnum.PUBLISH).identifier(requestBodyDTO.getIdentificativoDoc()).
						workflowInstanceId(requestBodyDTO.getWorkflowInstanceId()).build();
				
				DestinationEnum destination = DestinationEnum.fromString(requestBodyDTO.getDestination());
				out = brokerClient.dispatchAndSendData(request,destination);
			} catch (Exception ex) {
				out.setExClassCanonicalName(ExceptionUtils.getRootCause(ex).getClass().getCanonicalName());
				out.setMessageError(ex.getMessage());
			}
		}

		if(out != null && out.isEsito() && configSRV.isRemoveMetadataEnable()) {
			edsInvocationRepo.removeByWorkflowInstanceId(requestBodyDTO.getWorkflowInstanceId());
		}

		return out;
	}

	@Override
	public EdsResponseDTO deleteByIdentifier(final String identifier,final String fiscalCode,DestinationEnum destinationEnum) {
		EdsResponseDTO out = new EdsResponseDTO();
		try {
			BrokerRequestDTO broker = BrokerRequestDTO.builder().updateReqDTO(null).iniEdsInvocationETY(null)
			.identifier(identifier).operation(ProcessorOperationEnum.DELETE).fiscalCode(fiscalCode).build();
			out = brokerClient.dispatchAndSendData(broker,destinationEnum);
		} catch (Exception ex) {
			log.error("Error while running delete by identifier : ", ex);
			throw new BusinessException(ex);
		}
		return out;
	}

	@Override
	public EdsResponseDTO replaceByWorkflowInstanceIdAndIdentifier(String identifier, String workflowInstanceId,
			DestinationEnum destinationEnum) {
		EdsResponseDTO out = new EdsResponseDTO();

		IniEdsInvocationETY iniEdsInvocationETY = edsInvocationRepo.findByWorkflowInstanceId(workflowInstanceId);
		if (iniEdsInvocationETY != null && iniEdsInvocationETY.getData() != null) {

			BrokerRequestDTO req = BrokerRequestDTO.builder()
				.updateReqDTO(null)
				.iniEdsInvocationETY(iniEdsInvocationETY)
				.operation(ProcessorOperationEnum.REPLACE)
				.identifier(identifier)
				.workflowInstanceId(workflowInstanceId)
				.build();

			out = brokerClient.dispatchAndSendData(req,destinationEnum);
		}

		if(out != null && out.isEsito() && configSRV.isRemoveMetadataEnable()){
			edsInvocationRepo.removeByWorkflowInstanceId(workflowInstanceId);
		}

		return out;
	}

	@Override
	public EdsResponseDTO updateByRequest(String idDoc, EdsMetadataUpdateReqDTO updateReqDTO, DestinationEnum destinationEnum, String fiscalCode) {
		BrokerRequestDTO brokerRequestDto = BrokerRequestDTO.builder().updateReqDTO(updateReqDTO)
		.iniEdsInvocationETY(null).operation(ProcessorOperationEnum.UPDATE).fiscalCode(fiscalCode)
		.identifier(idDoc).build();
		
		return brokerClient.dispatchAndSendData(brokerRequestDto,destinationEnum);
		
	}
}
