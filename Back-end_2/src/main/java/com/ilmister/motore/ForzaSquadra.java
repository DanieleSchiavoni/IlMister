package com.ilmister.motore;

import java.util.EnumMap;
import java.util.Map;
import java.util.SplittableRandom;

import com.ilmister.entity.enumerazioni.Modulo;
import com.ilmister.entity.enumerazioni.Posizione;
import com.ilmister.entity.enumerazioni.Ruolo;
import com.ilmister.generazione.Attributi;
import com.ilmister.generazione.CalcolatoreAttributi;

/**
 * Come si misura la forza di una squadra in campo: tre zone (difesa, creazione, attacco) e il portiere.
 *
 * Ogni posizione contribuisce alle zone con dei pesi, e ogni giocatore con i propri attributi. I valori sono
 * poi divisi per quelli di una squadra "tipica" (giocatori di valutazione 58) con lo stesso modulo medio:
 * un indice 1,0 è una squadra media, 1,1 una squadra del dieci per cento più forte. I moduli offensivi hanno
 * un indice di attacco più alto e di difesa più basso, e viceversa.
 */
final class ForzaSquadra {

    static final int DIFESA = 0;
    static final int CREAZIONE = 1;
    static final int ATTACCO = 2;

    /** Il contributo di ogni posizione a [difesa, creazione, attacco] */
    private static final Map<Posizione, double[]> PESI_ZONA = new EnumMap<>(Posizione.class);

    static {
        PESI_ZONA.put(Posizione.PORTIERE, new double[] { 0, 0, 0 });
        double[] terzino = { 0.7, 0.15, 0.15 };
        PESI_ZONA.put(Posizione.TERZINO_DESTRO, terzino);
        PESI_ZONA.put(Posizione.TERZINO_SINISTRO, terzino);
        PESI_ZONA.put(Posizione.DIFENSORE_CENTRALE, new double[] { 1.0, 0, 0 });
        PESI_ZONA.put(Posizione.MEDIANO, new double[] { 0.6, 0.4, 0 });
        PESI_ZONA.put(Posizione.CENTROCAMPISTA, new double[] { 0.25, 0.6, 0.15 });
        PESI_ZONA.put(Posizione.TREQUARTISTA, new double[] { 0, 0.5, 0.5 });
        double[] esterno = { 0.2, 0.4, 0.4 };
        PESI_ZONA.put(Posizione.ESTERNO_DESTRO, esterno);
        PESI_ZONA.put(Posizione.ESTERNO_SINISTRO, esterno);
        double[] ala = { 0, 0.25, 0.75 };
        PESI_ZONA.put(Posizione.ALA_DESTRA, ala);
        PESI_ZONA.put(Posizione.ALA_SINISTRA, ala);
        PESI_ZONA.put(Posizione.ATTACCANTE, new double[] { 0, 0.1, 0.9 });
    }

    /** I valori di riferimento: la somma di zona di una squadra tipica, mediata sui moduli, e il portiere tipico */
    private static final double[] RIFERIMENTO = new double[3];
    private static double riferimentoPortiere;

    /** Quanto il modulo sposta ogni zona per una squadra tipica (1,0 = nessuno spostamento) */
    private static final Map<Modulo, double[]> SPOSTAMENTO_MODULO = new EnumMap<>(Modulo.class);

    /** Si attenua lo spostamento dei moduli: un 5-3-2 difende di più, ma non deve essere un modulo da buttare */
    private static final double ATTENUAZIONE_MODULO = 0.4;

    static {
        // Una squadra tipica: per ogni posizione, la media degli attributi di molti giocatori di valutazione 58
        SplittableRandom casuale = new SplittableRandom(20260101L);
        Map<Posizione, int[]> tipici = new EnumMap<>(Posizione.class);
        for (Posizione posizione : Posizione.values()) {
            long[] somma = new long[10];
            int campioni = 300;
            for (int i = 0; i < campioni; i++) {
                int[] valori = CalcolatoreAttributi.genera(posizione, 58, casuale).comeVettore();
                for (int k = 0; k < 10; k++) {
                    somma[k] += valori[k];
                }
            }
            int[] media = new int[10];
            for (int k = 0; k < 10; k++) {
                media[k] = (int) Math.round(somma[k] / (double) campioni);
            }
            tipici.put(posizione, media);
        }
        for (Modulo modulo : Modulo.values()) {
            for (Posizione slot : modulo.getPosti()) {
                Attributi tipico = Attributi.daVettore(tipici.get(slot));
                for (int zona = 0; zona < 3; zona++) {
                    RIFERIMENTO[zona] += PESI_ZONA.get(slot)[zona] * contributo(zona, tipico) / Modulo.values().length;
                }
            }
        }
        riferimentoPortiere = forzaPortiere(Attributi.daVettore(tipici.get(Posizione.PORTIERE)));

        for (Modulo modulo : Modulo.values()) {
            double[] somme = new double[3];
            for (Posizione slot : modulo.getPosti()) {
                Attributi tipico = Attributi.daVettore(tipici.get(slot));
                for (int zona = 0; zona < 3; zona++) {
                    somme[zona] += PESI_ZONA.get(slot)[zona] * contributo(zona, tipico);
                }
            }
            double[] spostamento = new double[3];
            for (int zona = 0; zona < 3; zona++) {
                spostamento[zona] = somme[zona] / RIFERIMENTO[zona];
            }
            SPOSTAMENTO_MODULO.put(modulo, spostamento);
        }
    }

    private ForzaSquadra() {
    }

    static double peso(Posizione slot, int zona) {
        return PESI_ZONA.get(slot)[zona];
    }

    /** Quanto un giocatore vale in una zona, in base ai suoi attributi (circa da 1 a 99) */
    static double contributo(int zona, Attributi a) {
        return switch (zona) {
            case ATTACCO -> 0.35 * a.tiro() + 0.20 * a.dribbling() + 0.15 * a.velocita() + 0.15 * a.tecnica()
                    + 0.15 * a.passaggio();
            case CREAZIONE -> 0.40 * a.passaggio() + 0.30 * a.tecnica() + 0.30 * a.intelligenza();
            default -> 0.40 * a.contrasto() + 0.25 * a.forza() + 0.20 * a.intelligenza() + 0.15 * a.velocita();
        };
    }

    static double forzaPortiere(Attributi a) {
        return 0.8 * a.parate() + 0.2 * a.intelligenza();
    }

    /**
     * Riporta una somma di zona a un indice dove 1,0 è la squadra tipica. Il modulo conta solo in parte:
     * si divide per il suo spostamento tipico, elevato a un esponente piccolo.
     */
    static double indice(Modulo modulo, int zona, double somma) {
        return somma / RIFERIMENTO[zona] / Math.pow(SPOSTAMENTO_MODULO.get(modulo)[zona], 1 - ATTENUAZIONE_MODULO);
    }

    static double indicePortiere(double forza) {
        return forza / riferimentoPortiere;
    }

    /** Quanto rende un giocatore fuori dalla sua posizione naturale */
    static double adattamento(Posizione slot, Posizione naturale) {
        if (slot == naturale) {
            return 1.0;
        }
        if (slot == Posizione.PORTIERE) {
            return 0.4; // un giocatore di movimento in porta
        }
        if (naturale == Posizione.PORTIERE) {
            return 0.5; // un portiere in campo
        }
        Ruolo a = slot.getRuolo();
        Ruolo b = naturale.getRuolo();
        if (a == b) {
            return 0.95;
        }
        boolean vicini = (a == Ruolo.CENTROCAMPISTA || b == Ruolo.CENTROCAMPISTA);
        return vicini ? 0.85 : 0.75;
    }
}
