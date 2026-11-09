package com.ilmister.exception;

/** Credenziali o sessione non valide (401); il messaggio resta volutamente generico */
public class CredenzialiNonValideException extends RuntimeException {

    public CredenzialiNonValideException(String messaggio) {
        super(messaggio);
    }
}
