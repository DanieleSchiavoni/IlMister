package com.ilmister.motore;

/** Le due squadre di una partita */
public enum Lato {
    CASA,
    TRASFERTA;

    public Lato avversario() {
        return this == CASA ? TRASFERTA : CASA;
    }
}
