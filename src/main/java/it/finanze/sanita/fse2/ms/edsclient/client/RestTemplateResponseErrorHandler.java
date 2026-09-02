package it.finanze.sanita.fse2.ms.edsclient.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;
import org.apache.commons.io.IOUtils;
import com.google.gson.Gson;

import it.finanze.sanita.fse2.ms.edsclient.dto.response.ErrorResponseDTO;
import it.finanze.sanita.fse2.ms.edsclient.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.edsclient.exceptions.DocumentNotFoundException;
import it.finanze.sanita.fse2.ms.edsclient.exceptions.InternalServerException;
@Component
public class RestTemplateResponseErrorHandler implements ResponseErrorHandler{

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
       int statusCode = response.getStatusCode().value();
       return (statusCode >= 400 && statusCode < 500) || 
	           (statusCode >= 500 && statusCode < 600);

    }

    @Override
    public void handleError(ClientHttpResponse httpResponse) throws IOException{
        String result = IOUtils.toString(httpResponse.getBody(), StandardCharsets.UTF_8);
		ErrorResponseDTO error = new Gson().fromJson(result, ErrorResponseDTO.class);

        if(httpResponse.getStatusCode() == HttpStatus.NOT_FOUND){
            if (error != null){
                String msg = error.getDetail() != null ? error.getDetail() : "Resource Not Found";
                throw new DocumentNotFoundException(msg);
            }
            
            
        }else if(httpResponse.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR){
            if (error != null){
                String msg = error.getDetail() != null ? error.getDetail() : "Internal Server Error";
                throw new InternalServerException(msg);
            }
        }else {
            if (error != null){
                String msg = error.getDetail() != null ? error.getDetail() : "Generic Error";
                throw new BusinessException(msg);
            }
        }
    
}
}