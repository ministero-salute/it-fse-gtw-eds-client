package it.finanze.sanita.fse2.ms.edsclient.logging;

public interface FailedDeliveryCallback<E> {
	
    void onFailedDelivery(E evt, Throwable throwable);
    
}