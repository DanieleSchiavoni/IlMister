package com.ilmister.exception;

/** La richiesta è corretta nella forma ma non si può eseguire (400) */
public class OperazioneNonValidaException extends RuntimeException {

    public OperazioneNonValidaException(String messaggio) {
        super(messaggio);
    }
}
