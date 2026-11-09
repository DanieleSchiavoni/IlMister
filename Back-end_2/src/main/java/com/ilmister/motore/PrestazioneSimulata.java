package com.ilmister.motore;

/** Ciò che un giocatore ha fatto: chi non è mai entrato in campo non compare */
public record PrestazioneSimulata(long giocatoreId, Lato lato, boolean titolare, int minutiGiocati, int gol,
        int assist, int tiri, boolean ammonito, boolean espulso, double voto, int energiaFinale) {
}
