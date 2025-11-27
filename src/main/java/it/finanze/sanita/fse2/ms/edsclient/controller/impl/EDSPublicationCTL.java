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
package it.finanze.sanita.fse2.ms.edsclient.controller.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import it.finanze.sanita.fse2.ms.edsclient.controller.IEDSPublicationCTL;
import it.finanze.sanita.fse2.ms.edsclient.dto.EdsResponseDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.DocumentRequestDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.EdsMetadataUpdateReqDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.PublicationRequestBodyDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.edsclient.service.IEdsInvocationSRV;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Eds Publication controller.
 */
@Slf4j
@RestController
public class EDSPublicationCTL extends AbstractCTL implements IEDSPublicationCTL {

    @Autowired
    private transient IEdsInvocationSRV edsInvocationSRV;

    @Override
    public EdsResponseDTO publish(final PublicationRequestBodyDTO requestBody, HttpServletRequest request) {

        final LogTraceInfoDTO traceInfoDTO = getLogTraceInfo();
        log.info("[START] {}() with arguments {}={}, {}={}", "publication", "traceId", traceInfoDTO.getTraceID(), "wif",
                requestBody.getWorkflowInstanceId());

        EdsResponseDTO out = edsInvocationSRV.publish(requestBody.getIdentificativoDoc(), requestBody.getWorkflowInstanceId());

        log.info("[EXIT] {}() with arguments {}={}, {}={}", "publication", "traceId", traceInfoDTO.getTraceID(), "wif",
                requestBody.getWorkflowInstanceId());
        return out;
    }

    @Override
    public EdsResponseDTO replace(final String idDoc, final DocumentRequestDTO replaceInfo,
            final HttpServletRequest request) {

        final LogTraceInfoDTO traceInfoDTO = getLogTraceInfo();
        log.info("[START] {}() with arguments {}={}, {}={}, {}={}", "replace", "traceId", traceInfoDTO.getTraceID(),
                "wif", replaceInfo.getWorkflowInstanceId(), "idDoc", replaceInfo.getIdDoc());

        EdsResponseDTO out = edsInvocationSRV.replace(replaceInfo.getIdDoc(), replaceInfo.getWorkflowInstanceId());

        log.info("[EXIT] {}() with arguments {}={}, {}={}, {}={}", "replace", "traceId", traceInfoDTO.getTraceID(),
                "wif", replaceInfo.getWorkflowInstanceId(), "idDoc", replaceInfo.getIdDoc());
        return out;
    }

    @Override
    public EdsResponseDTO delete(String ooid, String fiscalCode, HttpServletRequest request) {
        final LogTraceInfoDTO traceInfoDTO = getLogTraceInfo();

        log.info("[START] {}() with arguments {}={}", "delete", "traceId", traceInfoDTO.getTraceID());
        EdsResponseDTO out = edsInvocationSRV.delete(ooid, fiscalCode);
        log.info("[EXIT] {}() with arguments {}={}", "delete", "traceId", traceInfoDTO.getTraceID());

        return out;
    }

    @Override
    public EdsResponseDTO update(String idDoc, EdsMetadataUpdateReqDTO dto, HttpServletRequest request) {
        final LogTraceInfoDTO traceInfoDTO = getLogTraceInfo();

        log.info("[START] {}() with arguments {}={}, {}={}, {}={}", "update", "traceId", traceInfoDTO.getTraceID(),
                "wif", dto.getWorkflowInstanceId(), "idDoc", idDoc);
        EdsResponseDTO output = edsInvocationSRV.update(idDoc, dto, dto.getFiscalCode());
        log.info("[EXIT] {}() with arguments {}={}, {}={}, {}={}", "update", "traceId", traceInfoDTO.getTraceID(),
                "wif", dto.getWorkflowInstanceId(), "idDoc", idDoc);

        return output;
    }

}
