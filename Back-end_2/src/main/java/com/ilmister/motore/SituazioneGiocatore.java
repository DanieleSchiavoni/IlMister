package com.ilmister.motore;

/** Com'è un giocatore a metà partita: serve all'allenatore per decidere i cambi all'intervallo */
public record SituazioneGiocatore(long giocatoreId, Lato lato, boolean inCampo, boolean espulso, boolean ammonito,
        boolean infortunato, int energia, int gol, int assist, int tiri) {
}
