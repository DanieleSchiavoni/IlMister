package com.ilmister.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import com.ilmister.dto.AnteprimaCampionatoResponse;
import com.ilmister.dto.AnteprimaGiocatoreResponse;
import com.ilmister.dto.AnteprimaRosaResponse;
import com.ilmister.exception.RisorsaNonTrovataException;
import com.ilmister.generazione.GeneratoreCampionato;
import com.ilmister.service.impl.AnteprimaServiceImpl;

class AnteprimaServiceImplTest {

    private final Clock orologio = Clock.fixed(Instant.parse("2026-10-31T12:00:00Z"), ZoneOffset.UTC);
    private final AnteprimaService servizio = new AnteprimaServiceImpl(new GeneratoreCampionato(), orologio);

    @Test
    void anteprimaMostraLeSquadreConIMiglioriTreGiocatori() {
        AnteprimaCampionatoResponse anteprima = servizio.anteprima(7);

        assertThat(anteprima.seme()).isEqualTo(7);
        assertThat(anteprima.squadre()).hasSize(10);
        assertThat(anteprima.squadre().get(0).indice()).isZero();
        assertThat(anteprima.squadre().get(9).indice()).isEqualTo(9);
        anteprima.squadre().forEach(squadra -> {
            assertThat(squadra.migliori()).hasSize(3);
            // I migliori sono in ordine decrescente di valutazione
            AnteprimaGiocatoreResponse primo = squadra.migliori().get(0);
            AnteprimaGiocatoreResponse terzo = squadra.migliori().get(2);
            assertThat(primo.valutazione()).isGreaterThanOrEqualTo(terzo.valutazione());
        });
    }

    @Test
    void anteprimaESempreLaStessaPerLoStessoSeme() {
        assertThat(servizio.anteprima(21)).isEqualTo(servizio.anteprima(21));
        assertThat(servizio.anteprima(21)).isNotEqualTo(servizio.anteprima(22));
    }

    @Test
    void laRosaDiUnaSquadraHaTuttiIGiocatoriConEtaCoerente() {
        AnteprimaRosaResponse rosa = servizio.rosa(7, 3);

        assertThat(rosa.squadra()).isEqualTo(servizio.anteprima(7).squadre().get(3).nome());
        assertThat(rosa.giocatori()).hasSize(26);
        rosa.giocatori().forEach(giocatore -> assertThat(giocatore.eta()).isBetween(17, 38));
    }

    @Test
    void unaSquadraInesistenteNonSiTrova() {
        assertThatThrownBy(() -> servizio.rosa(7, 10)).isInstanceOf(RisorsaNonTrovataException.class);
        assertThatThrownBy(() -> servizio.rosa(7, -1)).isInstanceOf(RisorsaNonTrovataException.class);
    }
}
