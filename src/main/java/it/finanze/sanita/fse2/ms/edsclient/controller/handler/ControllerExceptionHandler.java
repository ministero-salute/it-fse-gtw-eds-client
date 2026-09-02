package it.finanze.sanita.fse2.ms.edsclient.controller.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import it.finanze.sanita.fse2.ms.edsclient.dto.response.ErrorResponseDTO;
import it.finanze.sanita.fse2.ms.edsclient.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.edsclient.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.edsclient.exceptions.DocumentNotFoundException;
import it.finanze.sanita.fse2.ms.edsclient.exceptions.InternalServerException;

@ControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler{

    @Autowired
	private Tracer tracer;

    @ExceptionHandler(value = {DocumentNotFoundException.class})
    protected ResponseEntity<ErrorResponseDTO> handleDocumentNotFoundException(final DocumentNotFoundException ex){

        String msg = ex.getMessage();
        ErrorResponseDTO out = new ErrorResponseDTO(getLogTraceInfo(), "/msg/record-not-found", "Record non trovato", msg, HttpStatus.NOT_FOUND.value(),"/record-not-found");
        int status = HttpStatus.NOT_FOUND.value();

        HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        
        return new ResponseEntity<>(out, headers,status );

        
    }

	@ExceptionHandler(value = {InternalServerException.class})
    protected ResponseEntity<ErrorResponseDTO> handleInteralServerException(final InternalServerException ex){

        String msg = ex.getMessage();
        ErrorResponseDTO out = new ErrorResponseDTO(getLogTraceInfo(), "/msg/internal-server-error", "Server non raggiungibile", msg, HttpStatus.INTERNAL_SERVER_ERROR.value(),"/internal-server-error");
        int status = HttpStatus.INTERNAL_SERVER_ERROR.value();

        HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        
        return new ResponseEntity<>(out, headers,status );

        
    }

    @ExceptionHandler(value = {BusinessException.class})
    protected ResponseEntity<ErrorResponseDTO> handleBusinessException(final BusinessException ex){

        String msg = ex.getMessage();
        ErrorResponseDTO out = new ErrorResponseDTO(getLogTraceInfo(), "/msg/generic-error", "Errore generico", msg, HttpStatus.INTERNAL_SERVER_ERROR.value(),"/generic-error");
        int status = HttpStatus.INTERNAL_SERVER_ERROR.value();

        HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        
        return new ResponseEntity<>(out, headers,status );

        
    }


    protected LogTraceInfoDTO getLogTraceInfo() {
		LogTraceInfoDTO out = new LogTraceInfoDTO(null, null);
		SpanBuilder spanbuilder = tracer.spanBuilder("EDS-CLIENT");
		
		if (spanbuilder != null) {
			out = new LogTraceInfoDTO(
					spanbuilder.startSpan().getSpanContext().getSpanId(), 
					spanbuilder.startSpan().getSpanContext().getTraceId());
		}
		return out;
	}

    
}
