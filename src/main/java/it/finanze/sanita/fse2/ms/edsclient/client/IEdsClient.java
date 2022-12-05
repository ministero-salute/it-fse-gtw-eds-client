/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.edsclient.client;


import it.finanze.sanita.fse2.ms.edsclient.dto.EdsResponseDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.request.IngestorRequestDTO;

/**
 * Interface of Eds client.
 */
public interface IEdsClient {

    EdsResponseDTO dispatchAndSendData(IngestorRequestDTO ingestorRequestDTO);
}
