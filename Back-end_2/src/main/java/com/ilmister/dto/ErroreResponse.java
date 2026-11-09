package com.ilmister.dto;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/** La risposta standard di tutti gli errori dell'API */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErroreResponse(Instant timestamp, int status, String errore, String messaggio, String percorso,
        List<ErroreCampo> campi) {

    public static ErroreResponse di(int status, String errore, String messaggio, String percorso) {
        return new ErroreResponse(Instant.now(), status, errore, messaggio, percorso, List.of());
    }
}
