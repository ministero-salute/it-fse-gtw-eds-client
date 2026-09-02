package it.finanze.sanita.fse2.ms.edsclient.exceptions;

public class InternalServerException extends RuntimeException {

    /**
     * Complete constructor.
     *
     * @param msg	Message to be shown.
     *              It should describe what the operation was trying to accomplish.
     */
    public InternalServerException(final String msg) {
        super(msg);
        
    }
    
}
