package com.ilmister.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.ilmister.entity.enumerazioni.FocusAllenamento;
import com.ilmister.entity.enumerazioni.Posizione;
import com.ilmister.generazione.Attributo;
import com.ilmister.generazione.Attributi;
import com.ilmister.generazione.CalcolatoreAttributi;
import com.ilmister.service.impl.RegoleAllenamento.Progresso;

class RegoleAllenamentoTest {

    private static int[] attributi(int valore) {
        int[] vettore = new int[10];
        Arrays.fill(vettore, valore);
        return vettore;
    }

    @Test
    void iGiovaniMiglioranoPiuDeiVeterani() {
        assertThat(RegoleAllenamento.probabilita(19)).isGreaterThan(RegoleAllenamento.probabilita(22));
        assertThat(RegoleAllenamento.probabilita(22)).isGreaterThan(RegoleAllenamento.probabilita(26));
        assertThat(RegoleAllenamento.probabilita(26)).isGreaterThan(RegoleAllenamento.probabilita(30));
        assertThat(RegoleAllenamento.probabilita(30)).isGreaterThan(RegoleAllenamento.probabilita(35));
    }

    @Test
    void ilFocusSceglieGliAttributiGiusti() {
        assertThat(RegoleAllenamento.candidati(FocusAllenamento.TECNICO, Posizione.CENTROCAMPISTA))
                .isSubsetOf(Attributo.TECNICA, Attributo.PASSAGGIO, Attributo.DRIBBLING);
        assertThat(RegoleAllenamento.candidati(FocusAllenamento.FISICO, Posizione.ATTACCANTE))
                .isSubsetOf(Attributo.VELOCITA, Attributo.RESISTENZA, Attributo.FORZA);
    }

    @Test
    void ilFocusNonDaAttributiInutiliAlRuolo() {
        // Un portiere non cresce nel tiro, un attaccante non nelle parate
        assertThat(RegoleAllenamento.candidati(FocusAllenamento.EQUILIBRATO, Posizione.PORTIERE))
                .doesNotContain(Attributo.TIRO);
        assertThat(RegoleAllenamento.candidati(FocusAllenamento.EQUILIBRATO, Posizione.ATTACCANTE))
                .doesNotContain(Attributo.PARATE);
        for (Posizione posizione : Posizione.values()) {
            for (FocusAllenamento focus : FocusAllenamento.values()) {
                for (Attributo attributo : RegoleAllenamento.candidati(focus, posizione)) {
                    assertThat(CalcolatoreAttributi.peso(posizione, attributo)).isPositive();
                }
            }
        }
    }

    @Test
    void chiHaRaggiuntoIlPotenzialeNonMigliora() {
        for (long seme = 0; seme < 200; seme++) {
            assertThat(RegoleAllenamento.prova(FocusAllenamento.TECNICO, Posizione.CENTROCAMPISTA, 19,
                    attributi(60), 70, 70, seme)).isEmpty();
        }
    }

    @Test
    void conLoStessoSemeIlRisultatoELoStesso() {
        for (long seme = 0; seme < 50; seme++) {
            Optional<Progresso> primo = RegoleAllenamento.prova(FocusAllenamento.TECNICO, Posizione.CENTROCAMPISTA,
                    19, attributi(60), 60, 80, seme);
            Optional<Progresso> secondo = RegoleAllenamento.prova(FocusAllenamento.TECNICO, Posizione.CENTROCAMPISTA,
                    19, attributi(60), 60, 80, seme);
            assertThat(primo).isEqualTo(secondo);
        }
    }

    @Test
    void ilMiglioramentoEDiUnPuntoSuUnAttributoDelFocus() {
        int migliorati = 0;
        for (long seme = 0; seme < 500; seme++) {
            Optional<Progresso> progresso = RegoleAllenamento.prova(FocusAllenamento.TECNICO, Posizione.CENTROCAMPISTA,
                    19, attributi(60), 60, 80, seme);
            if (progresso.isPresent()) {
                migliorati++;
                assertThat(progresso.get().nuovoValore()).isEqualTo(61);
                assertThat(progresso.get().attributo())
                        .isIn(Attributo.TECNICA, Attributo.PASSAGGIO, Attributo.DRIBBLING);
                assertThat(progresso.get().nuovaValutazione()).isBetween(60, 80);
            }
        }
        // Un diciannovenne migliora circa quattro giornate su dieci
        assertThat(migliorati).isBetween(150, 250);
    }

    @Test
    void laValutazioneNonSuperaMaiIlPotenziale() {
        for (long seme = 0; seme < 500; seme++) {
            RegoleAllenamento.prova(FocusAllenamento.EQUILIBRATO, Posizione.ATTACCANTE, 18, attributi(60), 69, 70,
                    seme).ifPresent(p -> assertThat(p.nuovaValutazione()).isLessThanOrEqualTo(70));
        }
    }

    @Test
    void unAttributoAlMassimoNonCresceOltre() {
        int[] vettore = attributi(CalcolatoreAttributi.MASSIMO);
        for (long seme = 0; seme < 200; seme++) {
            assertThat(RegoleAllenamento.prova(FocusAllenamento.EQUILIBRATO, Posizione.ATTACCANTE, 18, vettore, 60,
                    99, seme)).isEmpty();
        }
    }

    @Test
    void laValutazioneCalcolataCresceInsiemeAgliAttributi() {
        Attributi prima = Attributi.daVettore(attributi(60));
        int base = CalcolatoreAttributi.valutazione(Posizione.CENTROCAMPISTA, prima);

        for (long seme = 0; seme < 100; seme++) {
            RegoleAllenamento.prova(FocusAllenamento.TECNICO, Posizione.CENTROCAMPISTA, 19, attributi(60), base, 90,
                    seme).ifPresent(p -> assertThat(p.nuovaValutazione()).isGreaterThanOrEqualTo(base));
        }
    }
}
