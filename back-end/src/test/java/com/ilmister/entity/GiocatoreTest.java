package com.ilmister.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.ilmister.entity.enumerazioni.Posizione;
import com.ilmister.entity.enumerazioni.Ruolo;

class GiocatoreTest {

    private Giocatore giocatore() {
        Giocatore giocatore = new Giocatore();
        giocatore.setNome("Mario");
        giocatore.setCognome("Rossi");
        giocatore.setDataNascita(LocalDate.of(2000, 1, 1));
        giocatore.setPosizione(Posizione.DIFENSORE_CENTRALE);
        return giocatore;
    }

    @Test
    void etaSiCalcolaAllaDataDelGioco() {
        Giocatore giocatore = giocatore();

        assertThat(giocatore.etaAl(LocalDate.of(2025, 12, 31))).isEqualTo(25);
        assertThat(giocatore.etaAl(LocalDate.of(2026, 1, 1))).isEqualTo(26);
        assertThat(giocatore.etaAl(LocalDate.of(2026, 12, 31))).isEqualTo(26);
    }

    @Test
    void infortunioDuraFinoAlGiornoIndicatoCompreso() {
        Giocatore giocatore = giocatore();
        giocatore.setInfortunatoFinoAl(LocalDate.of(2026, 8, 10));

        assertThat(giocatore.infortunatoAl(LocalDate.of(2026, 8, 9))).isTrue();
        assertThat(giocatore.infortunatoAl(LocalDate.of(2026, 8, 10))).isTrue();
        assertThat(giocatore.infortunatoAl(LocalDate.of(2026, 8, 11))).isFalse();
    }

    @Test
    void senzaInfortunioIlGiocatoreEInSalute() {
        assertThat(giocatore().infortunatoAl(LocalDate.of(2026, 8, 10))).isFalse();
    }

    @Test
    void unGiocatoreSqualificatoOInfortunatoNonEDisponibile() {
        LocalDate oggi = LocalDate.of(2026, 9, 1);
        Giocatore giocatore = giocatore();
        assertThat(giocatore.disponibileAl(oggi)).isTrue();

        giocatore.setSqualificaGiornate(1);
        assertThat(giocatore.disponibileAl(oggi)).isFalse();

        giocatore.setSqualificaGiornate(0);
        giocatore.setInfortunatoFinoAl(LocalDate.of(2026, 9, 15));
        assertThat(giocatore.disponibileAl(oggi)).isFalse();
    }

    @Test
    void ilRepartoEIlNomeCompletoSiRicavanoDaiDati() {
        Giocatore giocatore = giocatore();

        assertThat(giocatore.getRuolo()).isEqualTo(Ruolo.DIFENSORE);
        assertThat(giocatore.nomeCompleto()).isEqualTo("Mario Rossi");
    }
}
