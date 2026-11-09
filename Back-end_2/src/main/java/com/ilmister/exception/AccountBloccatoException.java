package com.ilmister.exception;

/** L'account esiste ma un amministratore lo ha bloccato (403) */
public class AccountBloccatoException extends RuntimeException {

    public AccountBloccatoException() {
        super("Il tuo account è stato bloccato");
    }
}
