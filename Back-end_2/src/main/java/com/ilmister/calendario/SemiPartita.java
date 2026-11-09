package com.ilmister.calendario;

import java.util.SplittableRandom;

/** Il seme di ogni partita nasce da quello della carriera: a parità di seme il gioco è ripetibile */
public final class SemiPartita {

    private SemiPartita() {
    }

    /**
     * @param semeCarriera   il seme della carriera
     * @param giornata       il numero della giornata, da 1
     * @param indicePartita  la posizione della partita nella giornata, da 0
     */
    public static long seme(long semeCarriera, int giornata, int indicePartita) {
        return new SplittableRandom(semeCarriera * 1_000_003L + giornata * 1_009L + indicePartita).nextLong();
    }
}
