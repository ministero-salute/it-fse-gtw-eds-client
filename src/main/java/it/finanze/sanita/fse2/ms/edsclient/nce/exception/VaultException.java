package it.finanze.sanita.fse2.ms.edsclient.nce.exception;

public class VaultException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public VaultException(String message) {
        super(message);
    }

    public VaultException(String message, Throwable cause) {
        super(message, cause);
    }

    public VaultException(Throwable cause) {
        super(cause);
    }

}