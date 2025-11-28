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
    public EdsResponseDTO publish(String idDoc, String workflowInstanceId) {

        EdsResponseDTO out = new EdsResponseDTO();

        // Find document from DB
        IniEdsInvocationETY iniEdsInvocationETY = edsInvocationRepo.find(workflowInstanceId);
        if (iniEdsInvocationETY == null || iniEdsInvocationETY.getData() == null) {
            String messageError = "Nessun documento trovato per il workflowInstanceId: " + workflowInstanceId;
            out.setEsito(false);
            out.setMessageError(messageError);
            log.debug(messageError);
        }

        // Call EDS and send the document for publication
        if (StringUtils.isEmpty(out.getMessageError())) {
            try {
                BrokerRequestDTO request = BrokerRequestDTO.builder()
                        .updateReqDTO(null)
                        .iniEdsInvocationETY(iniEdsInvocationETY)
                        .operation(ProcessorOperationEnum.PUBLISH)
                        .identifier(idDoc)
                        .workflowInstanceId(workflowInstanceId)
                        .build();

                out = brokerClient.dispatchAndSendData(request);
            } catch (Exception ex) {
                out.setExClassCanonicalName(ExceptionUtils.getRootCause(ex).getClass().getCanonicalName());
                out.setMessageError(ex.getMessage());
            }
        }

        if (out != null && out.isEsito() && configSRV.isRemoveMetadataEnable()) {
            edsInvocationRepo.remove(workflowInstanceId);
        }

        return out;
    }

    @Override
    public EdsResponseDTO replace(String idDoc, String workflowInstanceId) {

        EdsResponseDTO out = new EdsResponseDTO();

        // Retrieve document from DB
        IniEdsInvocationETY iniEdsInvocationETY = edsInvocationRepo.find(workflowInstanceId);

        if (iniEdsInvocationETY == null || iniEdsInvocationETY.getData() == null) {
            String messageError = "Nessun documento trovato per il workflowInstanceId: " + workflowInstanceId;
            out.setEsito(false);
            out.setMessageError(messageError);
            log.debug(messageError);
        } else {

            BrokerRequestDTO req = BrokerRequestDTO.builder()
                    .updateReqDTO(null)
                    .iniEdsInvocationETY(iniEdsInvocationETY)
                    .operation(ProcessorOperationEnum.REPLACE)
                    .identifier(idDoc)
                    .workflowInstanceId(workflowInstanceId)
                    .build();

            out = brokerClient.dispatchAndSendData(req);
        }

        if (out != null && out.isEsito() && configSRV.isRemoveMetadataEnable()) {
            edsInvocationRepo.remove(workflowInstanceId);
        }

        return out;
    }

    @Override
    public EdsResponseDTO delete(final String identifier, final String fiscalCode) {
        EdsResponseDTO out = new EdsResponseDTO();
        try {
            BrokerRequestDTO broker = BrokerRequestDTO.builder().updateReqDTO(null).iniEdsInvocationETY(null)
                    .identifier(identifier).operation(ProcessorOperationEnum.DELETE).fiscalCode(fiscalCode).build();
            out = brokerClient.dispatchAndSendData(broker);
        } catch (Exception ex) {
            log.error("Error while running delete by identifier : ", ex);
            throw new BusinessException(ex);
        }
        return out;
    }

    @Override
    public EdsResponseDTO update(String idDoc, EdsMetadataUpdateReqDTO updateReqDTO, String fiscalCode) {
        BrokerRequestDTO brokerRequestDto = BrokerRequestDTO.builder().updateReqDTO(updateReqDTO)
                .iniEdsInvocationETY(null).operation(ProcessorOperationEnum.UPDATE).fiscalCode(fiscalCode)
                .identifier(idDoc).build();

        return brokerClient.dispatchAndSendData(brokerRequestDto);

    }
}
