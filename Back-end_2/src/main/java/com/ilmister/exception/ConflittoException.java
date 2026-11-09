package com.ilmister.exception;

/** La richiesta è corretta ma entra in conflitto con ciò che esiste già, ad esempio un username occupato (409) */
public class ConflittoException extends RuntimeException {

    public ConflittoException(String messaggio) {
        super(messaggio);
    }
}
