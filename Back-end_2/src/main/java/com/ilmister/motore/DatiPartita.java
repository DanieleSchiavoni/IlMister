package com.ilmister.motore;

/**
 * Tutto ciò che serve a giocare una partita. Con gli stessi dati e lo stesso seme la partita è sempre la stessa.
 */
public record DatiPartita(long seme, String stadio, SquadraInCampo casa, SquadraInCampo trasferta) {

    public SquadraInCampo squadra(Lato lato) {
        return lato == Lato.CASA ? casa : trasferta;
    }
}
