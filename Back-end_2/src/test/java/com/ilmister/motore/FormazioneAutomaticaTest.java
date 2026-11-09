package com.ilmister.motore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.ilmister.entity.enumerazioni.Modulo;
import com.ilmister.entity.enumerazioni.Posizione;
import com.ilmister.generazione.Attributi;
import com.ilmister.generazione.CalcolatoreAttributi;
import com.ilmister.generazione.SquadraGenerata;

class FormazioneAutomaticaTest {

    @Test
    void perOgniModuloEOgniRosaCiSonoUndiciTitolariNelPostoGiusto() {
        for (long seme = 1; seme <= 10; seme++) {
            for (SquadraGenerata squadra : SquadreDiProva.campionato(seme)) {
                for (Modulo modulo : Modulo.values()) {
                    FormazioneAutomatica.Scelta scelta = FormazioneAutomatica.scegli(SquadreDiProva.rosa(squadra, 0), modulo);

                    assertThat(scelta.titolari()).hasSize(11);
                    for (int i = 0; i < 11; i++) {
                        // La rosa generata copre ogni posizione di ogni modulo: ognuno gioca nel suo ruolo
                        assertThat(scelta.titolari().get(i).posizione()).as(modulo.getCodice() + " posto " + i)
                                .isEqualTo(modulo.getPosti().get(i));
                    }
                }
            }
        }
    }

    @Test
    void laPanchinaHaSettegiocatoriConUnPortiereENessunDoppione() {
        for (SquadraGenerata squadra : SquadreDiProva.campionato(3)) {
            FormazioneAutomatica.Scelta scelta = FormazioneAutomatica.scegli(SquadreDiProva.rosa(squadra, 0), Modulo.M_4_3_3);

            assertThat(scelta.panchina()).hasSize(FormazioneAutomatica.GIOCATORI_IN_PANCHINA);
            assertThat(scelta.panchina().stream().anyMatch(g -> g.posizione() == Posizione.PORTIERE)).isTrue();
            Set<Long> identificativi = new HashSet<>();
            scelta.titolari().forEach(g -> identificativi.add(g.id()));
            scelta.panchina().forEach(g -> identificativi.add(g.id()));
            assertThat(identificativi).hasSize(11 + FormazioneAutomatica.GIOCATORI_IN_PANCHINA);
        }
    }

    private GiocatoreInCampo giocatore(long id, Posizione posizione, int valutazione, int energia) {
        Attributi attributi = CalcolatoreAttributi.genera(posizione, valutazione, new java.util.SplittableRandom(id));
        return new GiocatoreInCampo(id, "G" + id, posizione, attributi, 50, 70, energia, (int) id);
    }

    @Test
    void traDueGiocatoriDelloStessoRuoloSiSceglieIlMigliore() {
        List<GiocatoreInCampo> rosa = new ArrayList<>(SquadreDiProva.rosa(SquadreDiProva.campionato(1).get(0), 0));
        GiocatoreInCampo fenomeno = giocatore(500, Posizione.ATTACCANTE, 95, 100);
        rosa.add(fenomeno);

        FormazioneAutomatica.Scelta scelta = FormazioneAutomatica.scegli(rosa, Modulo.M_4_4_2);

        assertThat(scelta.titolari().stream().anyMatch(g -> g.id() == 500)).isTrue();
    }

    @Test
    void unGiocatoreMoltoStancoVieneRimpiazzatoDaUnaRiservaFresca() {
        List<GiocatoreInCampo> rosa = new ArrayList<>(SquadreDiProva.rosa(SquadreDiProva.campionato(1).get(0), 0));
        GiocatoreInCampo stanco = giocatore(600, Posizione.ATTACCANTE, 70, 10);
        GiocatoreInCampo fresco = giocatore(601, Posizione.ATTACCANTE, 66, 100);
        rosa.add(stanco);
        rosa.add(fresco);

        FormazioneAutomatica.Scelta scelta = FormazioneAutomatica.scegli(rosa, Modulo.M_4_3_3);

        assertThat(scelta.titolari().stream().anyMatch(g -> g.id() == 601)).isTrue();
        assertThat(scelta.titolari().stream().anyMatch(g -> g.id() == 600)).isFalse();
    }

    @Test
    void senzaUnPortiereInPanchinaLaSceltaFunzionaComunque() {
        List<GiocatoreInCampo> rosa = new ArrayList<>(SquadreDiProva.rosa(SquadreDiProva.campionato(1).get(0), 0));
        rosa.removeIf(g -> g.posizione() == Posizione.PORTIERE && g.id() % 3 != 0);

        FormazioneAutomatica.Scelta scelta = FormazioneAutomatica.scegli(rosa, Modulo.M_4_3_3);

        assertThat(scelta.titolari()).hasSize(11);
    }

    @Test
    void conMenoDiUndiciGiocatoriLaSceltaNonEPossibile() {
        List<GiocatoreInCampo> pochi = SquadreDiProva.rosa(SquadreDiProva.campionato(1).get(0), 0).subList(0, 10);

        assertThatThrownBy(() -> FormazioneAutomatica.scegli(pochi, Modulo.M_4_3_3))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
