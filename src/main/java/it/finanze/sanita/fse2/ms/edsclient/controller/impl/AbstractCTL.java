package it.finanze.sanita.fse2.ms.edsclient.controller.impl;

import brave.Tracer;
import it.finanze.sanita.fse2.ms.edsclient.dto.response.LogTraceInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;

/**
 * 
 * @author vincenzoingenito
 *
 *	Abstract controller.
 */
@Slf4j
public abstract class AbstractCTL implements Serializable {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -3077780100650268134L;
	
	@Autowired
	private Tracer tracer;

        
	protected LogTraceInfoDTO getLogTraceInfo() {
		LogTraceInfoDTO out = new LogTraceInfoDTO(null, null);
		if (tracer.currentSpan() != null) {
			out = new LogTraceInfoDTO(
					tracer.currentSpan().context().spanIdString(), 
					tracer.currentSpan().context().traceIdString());
		}
		return out;
	}


}
