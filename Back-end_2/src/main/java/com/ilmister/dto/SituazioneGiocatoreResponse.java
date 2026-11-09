package com.ilmister.dto;

/** Com'è un giocatore della squadra dell'utente a metà partita */
public record SituazioneGiocatoreResponse(Long giocatoreId, String cognome, String siglaPosizione, boolean inCampo,
        boolean espulso, boolean ammonito, boolean infortunato, int energia, int gol, int assist, int tiri) {
}
