package it.finanze.sanita.fse2.ms.edsclient.client;


import it.finanze.sanita.fse2.ms.edsclient.dto.request.IngestorRequestDTO;

import java.io.Serializable;

/**
 * Interface of Eds client.
 * 
 * @author Riccardo Bonesi
 */
public interface IEdsClient extends Serializable {

    Boolean dispatchAndSendData(IngestorRequestDTO ingestorRequestDTO);
}
