package com.ilmister.generazione;

import java.util.EnumMap;
import java.util.Map;
import java.util.random.RandomGenerator;

import com.ilmister.entity.enumerazioni.Posizione;

/**
 * Lega tra loro posizione, attributi e valutazione complessiva.
 *
 * Ogni posizione ha dei pesi (da 0 a 6, somma 10) che dicono quali attributi contano: la valutazione di
 * un calciatore è la media di quegli attributi, pesata. Per generare un giocatore si parte dalla valutazione
 * che si vuole ottenere e si ricavano gli attributi, così che la media pesata torni esattamente a quel valore.
 * Questo permette di importare giocatori reali con un solo numero (la valutazione) e avere comunque tutti
 * gli attributi coerenti con il ruolo.
 */
public final class CalcolatoreAttributi {

    public static final int MINIMO = 1;
    public static final int MASSIMO = 99;

    // Ordine: velocità, resistenza, forza, tecnica, passaggio, tiro, dribbling, contrasto, intelligenza, parate
    private static final Map<Posizione, int[]> PESI = new EnumMap<>(Posizione.class);

    static {
        PESI.put(Posizione.PORTIERE, new int[] { 1, 0, 1, 0, 0, 0, 0, 0, 2, 6 });
        int[] terzino = { 2, 2, 1, 1, 1, 0, 0, 2, 1, 0 };
        PESI.put(Posizione.TERZINO_DESTRO, terzino);
        PESI.put(Posizione.TERZINO_SINISTRO, terzino);
        PESI.put(Posizione.DIFENSORE_CENTRALE, new int[] { 1, 0, 3, 0, 1, 0, 0, 3, 2, 0 });
        PESI.put(Posizione.MEDIANO, new int[] { 0, 2, 1, 0, 2, 0, 0, 3, 2, 0 });
        PESI.put(Posizione.CENTROCAMPISTA, new int[] { 0, 2, 0, 2, 3, 0, 0, 1, 2, 0 });
        PESI.put(Posizione.TREQUARTISTA, new int[] { 0, 0, 0, 3, 2, 2, 2, 0, 1, 0 });
        int[] esterno = { 2, 2, 0, 1, 2, 0, 2, 0, 1, 0 };
        PESI.put(Posizione.ESTERNO_DESTRO, esterno);
        PESI.put(Posizione.ESTERNO_SINISTRO, esterno);
        int[] ala = { 3, 0, 0, 1, 1, 2, 3, 0, 0, 0 };
        PESI.put(Posizione.ALA_DESTRA, ala);
        PESI.put(Posizione.ALA_SINISTRA, ala);
        PESI.put(Posizione.ATTACCANTE, new int[] { 1, 0, 1, 1, 0, 4, 1, 0, 2, 0 });
    }

    private CalcolatoreAttributi() {
    }

    /** Il peso di un attributo per la posizione indicata */
    public static int peso(Posizione posizione, Attributo attributo) {
        return PESI.get(posizione)[attributo.ordinal()];
    }

    /** La valutazione complessiva: la media degli attributi, pesata sulla posizione, arrotondata */
    public static int valutazione(Posizione posizione, Attributi attributi) {
        int[] pesi = PESI.get(posizione);
        int[] valori = attributi.comeVettore();
        int somma = 0;
        for (int i = 0; i < pesi.length; i++) {
            somma += pesi[i] * valori[i];
        }
        return Math.round(somma / 10f);
    }

    /**
     * Genera gli attributi di un giocatore con la valutazione richiesta: quelli importanti per la posizione
     * attorno a quel valore (con una variazione casuale), gli altri più bassi.
     */
    public static Attributi genera(Posizione posizione, int valutazioneObiettivo, RandomGenerator casuale) {
        int[] pesi = PESI.get(posizione);
        int[] valori = new int[pesi.length];
        for (int i = 0; i < pesi.length; i++) {
            if (pesi[i] > 0) {
                valori[i] = limita((int) Math.round(valutazioneObiettivo + casuale.nextGaussian() * 5));
            } else if (i == Attributo.PARATE.ordinal()) {
                valori[i] = 6 + casuale.nextInt(14); // chi non è portiere para poco
            } else {
                valori[i] = limita((int) Math.round(valutazioneObiettivo * 0.5 + casuale.nextGaussian() * 5));
            }
        }
        // Gli attributi che contano si spostano tutti insieme finché la media pesata coincide con l'obiettivo
        for (int giro = 0; giro < 8; giro++) {
            int differenza = valutazioneObiettivo - valutazione(posizione, Attributi.daVettore(valori));
            if (differenza == 0) {
                break;
            }
            for (int i = 0; i < pesi.length; i++) {
                if (pesi[i] > 0) {
                    valori[i] = limita(valori[i] + differenza);
                }
            }
        }
        return Attributi.daVettore(valori);
    }

    private static int limita(int valore) {
        return Math.max(MINIMO, Math.min(MASSIMO, valore));
    }
}
