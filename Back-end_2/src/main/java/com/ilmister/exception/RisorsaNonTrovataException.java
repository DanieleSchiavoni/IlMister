package com.ilmister.exception;

/** La risorsa non esiste oppure appartiene a un altro utente: da fuori le due cose non si distinguono (404) */
public class RisorsaNonTrovataException extends RuntimeException {

    public RisorsaNonTrovataException(String messaggio) {
        super(messaggio);
    }
}
