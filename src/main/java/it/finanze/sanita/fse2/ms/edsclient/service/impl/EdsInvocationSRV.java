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

import it.finanze.sanita.fse2.ms.edsclient.dto.response.GetDocumentReferenceResDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.edsclient.client.IBrokerClient;
import it.finanze.sanita.fse2.ms.edsclient.dto.EdsResponseDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.OptionalLogDataDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.EdsMetadataUpdateReqDTO;
import it.finanze.sanita.fse2.ms.edsclient.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.edsclient.utility.RequestUtility;
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

        IniEdsInvocationETY ety = edsInvocationRepo.find(workflowInstanceId);
        if (ety == null || ety.getData() == null) {
            String msg = "Nessun documento trovato per il workflowInstanceId: " + workflowInstanceId;
            out.setEsito(false);
            out.setMessageError(msg);
            log.debug(msg);
            return out;
        }

        log.debug("IniEdsInvocationETY - metadata: {}, fiscalCode: {}, rde: {}",
                ety.getMetadata(), ety.getFiscalCode(), ety.getRde());

        out = brokerClient.publish(idDoc, workflowInstanceId, ety,
                extractOptionalLogData(ety, idDoc, workflowInstanceId));

        if (out.isEsito() && configSRV.isRemoveMetadataEnable()) {
            edsInvocationRepo.remove(workflowInstanceId);
        }
        return out;
    }

    @Override
    public EdsResponseDTO replace(String idDoc, String workflowInstanceId) {
        EdsResponseDTO out = new EdsResponseDTO();

        IniEdsInvocationETY ety = edsInvocationRepo.find(workflowInstanceId);
        log.info("IniEdsInvocationETY (replace) - metadata: {}, fiscalCode: {}, rde: {}",
                ety != null ? ety.getMetadata() : "ETY NULL",
                ety != null ? ety.getFiscalCode() : "ETY NULL",
                ety != null ? ety.getRde() : "ETY NULL");

        if (ety == null || ety.getData() == null) {
            String msg = "Nessun documento trovato per il workflowInstanceId: " + workflowInstanceId;
            out.setEsito(false);
            out.setMessageError(msg);
            log.debug(msg);
            return out;
        }

        out = brokerClient.replace(idDoc, workflowInstanceId, ety,
                extractOptionalLogData(ety, idDoc, workflowInstanceId));

        if (out.isEsito() && configSRV.isRemoveMetadataEnable()) {
            edsInvocationRepo.remove(workflowInstanceId);
        }
        return out;
    }

    @Override
    public EdsResponseDTO delete(final String identifier, final String fiscalCode, final String jwt) {
        try {
            return brokerClient.delete(identifier, fiscalCode, jwt);
        } catch (Exception ex) {
            log.error("Errore durante delete per identifier: ", ex);
            throw new BusinessException(ex);
        }
    }

    @Override
    public EdsResponseDTO update(String idDoc, EdsMetadataUpdateReqDTO updateReqDTO, String fiscalCode, String jwt) {
        return brokerClient.update(idDoc, updateReqDTO, fiscalCode, jwt);
    }

    @Override
    public GetDocumentReferenceResDTO getDocumentReference(String masterIdentifier, String fiscalCode, String jwt) {
        return brokerClient.getDocumentReference(fiscalCode, masterIdentifier, jwt);
    }

    private OptionalLogDataDTO extractOptionalLogData(IniEdsInvocationETY invocation, String idDoc,
            String workflowInstanceId) {
        try {
            return RequestUtility.extractOptionalLogData(invocation, idDoc, workflowInstanceId);
        } catch (RuntimeException ex) {
            log.warn("Impossibile estrarre dati log strutturato opzionali", ex);
            return OptionalLogDataDTO.builder()
                    .fiscalCode(invocation == null ? null : invocation.getFiscalCode())
                    .documentId(idDoc)
                    .workflowInstanceId(workflowInstanceId)
                    .build();
        }
    }
}
