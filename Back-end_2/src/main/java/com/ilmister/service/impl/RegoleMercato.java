package com.ilmister.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.SplittableRandom;

/** Le regole numeriche del mercato: funzioni pure, facili da collaudare */
final class RegoleMercato {

    static final int ROSA_MINIMA = 16;
    static final int ROSA_MASSIMA = 30;
    static final int PORTIERI_MINIMI = 2;
    /** Una squadra del computer vende i suoi titolari a un prezzo più alto */
    static final double RINCARO_TITOLARI = 1.10;
    /** Chi compra dall'utente paga l'85% del valore di mercato */
    static final double QUOTA_VENDITA = 0.85;

    enum Giudizio { ACCETTATA, VICINA, LONTANA }

    private RegoleMercato() {
    }

    /**
     * Quanto chiede la squadra del computer per cedere un giocatore: dal 100% al 125% del valore, deciso dal seme
     * della carriera, dal giocatore e dal giorno (così la stessa offerta nello stesso giorno ha sempre lo stesso esito).
     */
    static BigDecimal soglia(BigDecimal valoreMercato, boolean titolare, long semeCarriera, long giocatoreId,
            LocalDate data) {
        long seme = semeCarriera ^ (giocatoreId * 0x9E3779B97F4A7C15L) ^ (data.toEpochDay() * 0xC2B2AE3D27D4EB4FL);
        double coefficiente = 1.0 + 0.25 * new SplittableRandom(seme).nextDouble();
        if (titolare) {
            coefficiente *= RINCARO_TITOLARI;
        }
        return arrotonda(valoreMercato.doubleValue() * coefficiente);
    }

    static Giudizio giudica(BigDecimal offerta, BigDecimal soglia) {
        if (offerta.compareTo(soglia) >= 0) {
            return Giudizio.ACCETTATA;
        }
        // Dal 90% in su la trattativa è "vicina"
        return offerta.compareTo(soglia.multiply(BigDecimal.valueOf(0.9))) >= 0 ? Giudizio.VICINA : Giudizio.LONTANA;
    }

    static BigDecimal prezzoVendita(BigDecimal valoreMercato) {
        return arrotonda(valoreMercato.doubleValue() * QUOTA_VENDITA);
    }

    /** Si arrotonda alle centinaia, come i valori di mercato */
    private static BigDecimal arrotonda(double valore) {
        return BigDecimal.valueOf(Math.round(valore / 100.0) * 100L).setScale(2, RoundingMode.UNNECESSARY);
    }
}
