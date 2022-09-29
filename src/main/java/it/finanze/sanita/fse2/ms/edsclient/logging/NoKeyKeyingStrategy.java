package it.finanze.sanita.fse2.ms.edsclient.logging;


public class NoKeyKeyingStrategy implements KeyingStrategy<Object> {

    @Override
    public byte[] createKey(Object e) {
        return null;
    }
} 