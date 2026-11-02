package com.ilmister.generazione;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.SplittableRandom;

import org.junit.jupiter.api.Test;

import com.ilmister.entity.enumerazioni.Posizione;

class CalcolatoreAttributiTest {

    @Test
    void ipesiDiOgniPosizioneSommanoDieci() {
        for (Posizione posizione : Posizione.values()) {
            int somma = 0;
            for (Attributo attributo : Attributo.values()) {
                somma += CalcolatoreAttributi.peso(posizione, attributo);
            }
            assertThat(somma).as(posizione.name()).isEqualTo(10);
        }
    }

    @Test
    void laValutazioneGeneraTornaSempreAQuellaRichiesta() {
        for (Posizione posizione : Posizione.values()) {
            for (int obiettivo = 30; obiettivo <= 90; obiettivo += 5) {
                for (long seme = 1; seme <= 25; seme++) {
                    Attributi attributi = CalcolatoreAttributi.genera(posizione, obiettivo, new SplittableRandom(seme));

                    int valutazione = CalcolatoreAttributi.valutazione(posizione, attributi);

                    assertThat(Math.abs(valutazione - obiettivo))
                            .as(posizione + " " + obiettivo + " seme " + seme).isLessThanOrEqualTo(1);
                }
            }
        }
    }

    @Test
    void gliAttributiRestanoSempreTraUnoENovantanove() {
        for (Posizione posizione : Posizione.values()) {
            for (int obiettivo : new int[] { 10, 30, 60, 90, 99 }) {
                for (int valore : CalcolatoreAttributi.genera(posizione, obiettivo, new SplittableRandom(3)).comeVettore()) {
                    assertThat(valore).as(posizione + " " + obiettivo)
                            .isBetween(CalcolatoreAttributi.MINIMO, CalcolatoreAttributi.MASSIMO);
                }
            }
        }
    }

    @Test
    void soloIlPortierePara() {
        SplittableRandom casuale = new SplittableRandom(11);
        for (Posizione posizione : Posizione.values()) {
            int parate = CalcolatoreAttributi.genera(posizione, 65, casuale).parate();
            if (posizione == Posizione.PORTIERE) {
                assertThat(parate).as("portiere").isGreaterThanOrEqualTo(50);
            } else {
                assertThat(parate).as(posizione.name()).isLessThan(25);
            }
        }
    }

    @Test
    void unAttaccanteTiraMeglioDiUnDifensore() {
        SplittableRandom casuale = new SplittableRandom(5);
        int sommaAttaccanti = 0;
        int sommaDifensori = 0;
        for (int i = 0; i < 200; i++) {
            sommaAttaccanti += CalcolatoreAttributi.genera(Posizione.ATTACCANTE, 60, casuale).tiro();
            sommaDifensori += CalcolatoreAttributi.genera(Posizione.DIFENSORE_CENTRALE, 60, casuale).tiro();
        }

        assertThat(sommaAttaccanti).isGreaterThan(sommaDifensori + 200 * 15);
    }

    @Test
    void lostessoSemeDaGliStessiAttributi() {
        Attributi primo = CalcolatoreAttributi.genera(Posizione.MEDIANO, 58, new SplittableRandom(99));
        Attributi secondo = CalcolatoreAttributi.genera(Posizione.MEDIANO, 58, new SplittableRandom(99));

        assertThat(primo).isEqualTo(secondo);
    }
}
