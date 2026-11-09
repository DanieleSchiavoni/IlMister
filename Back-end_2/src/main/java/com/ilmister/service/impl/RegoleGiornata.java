package com.ilmister.service.impl;

import java.util.SplittableRandom;

/** Le regole numeriche di ciò che cambia dopo una giornata; sono funzioni pure, facili da collaudare */
final class RegoleGiornata {

    /** L'energia che un giocatore recupera tra una giornata e l'altra */
    static final int RECUPERO_ENERGIA = 40;
    /** Le giornate di squalifica dopo un'espulsione */
    static final int GIORNATE_SQUALIFICA_ROSSO = 1;

    private RegoleGiornata() {
    }

    static int energiaRecuperata(int energia) {
        return Math.min(100, energia + RECUPERO_ENERGIA);
    }

    /** La forma sale con un buon voto e scende con uno scarso: un voto 6 la lascia com'è */
    static int nuovaForma(int forma, double voto) {
        return limita(forma + (int) Math.round((voto - 6.0) * 3));
    }

    /** Il morale cresce con una vittoria e cala con una sconfitta */
    static int nuovoMorale(int morale, int golFatti, int golSubiti) {
        int variazione = Integer.compare(golFatti, golSubiti) * 4;
        return limita(morale + variazione);
    }

    /** Il pubblico dipende dalla capienza e dalla reputazione della società, con un po' di variazione */
    static int spettatori(int capienza, int reputazione, long seme) {
        double riempimento = 0.30 + 0.55 * reputazione / 100.0;
        double variazione = 0.9 + 0.2 * new SplittableRandom(seme ^ 0x9E3779B97F4A7C15L).nextDouble();
        int spettatori = (int) Math.round(capienza * riempimento * variazione);
        return Math.max(0, Math.min(capienza, spettatori));
    }

    private static int limita(int valore) {
        return Math.max(1, Math.min(100, valore));
    }
}
