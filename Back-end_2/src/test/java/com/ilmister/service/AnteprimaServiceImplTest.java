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
import com.ilmister.dto.PartitaDiProvaResponse;
import com.ilmister.exception.OperazioneNonValidaException;
import com.ilmister.exception.RisorsaNonTrovataException;
import com.ilmister.motore.MotorePartita;
import com.ilmister.generazione.GeneratoreCampionato;
import com.ilmister.service.impl.AnteprimaServiceImpl;

class AnteprimaServiceImplTest {

    private final Clock orologio = Clock.fixed(Instant.parse("2026-10-31T12:00:00Z"), ZoneOffset.UTC);
    private final AnteprimaService servizio = new AnteprimaServiceImpl(new GeneratoreCampionato(), new MotorePartita(), orologio);

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

    @Test
    void laPartitaDiProvaHaRisultatoStatisticheECronaca() {
        PartitaDiProvaResponse partita = servizio.partitaDiProva(7, 0, 1, 42);

        assertThat(partita.casa()).isEqualTo(servizio.anteprima(7).squadre().get(0).nome());
        assertThat(partita.trasferta()).isEqualTo(servizio.anteprima(7).squadre().get(1).nome());
        assertThat(partita.cronaca().get(0)).startsWith("1' Fischio d'inizio");
        assertThat(partita.cronaca().get(partita.cronaca().size() - 1)).contains("Triplice fischio");
        assertThat(partita.statisticheCasa().tiri()).isGreaterThanOrEqualTo(partita.golCasa());
        assertThat(partita.statisticheTrasferta().tiri()).isGreaterThanOrEqualTo(partita.golTrasferta());
    }

    @Test
    void laStessaPartitaDiProvaEsceSempreUguale() {
        assertThat(servizio.partitaDiProva(7, 2, 3, 99)).isEqualTo(servizio.partitaDiProva(7, 2, 3, 99));
        assertThat(servizio.partitaDiProva(7, 2, 3, 99)).isNotEqualTo(servizio.partitaDiProva(7, 2, 3, 100));
    }

    @Test
    void lePartiteDiProvaNonValideSonoRifiutate() {
        assertThatThrownBy(() -> servizio.partitaDiProva(7, 4, 4, 1)).isInstanceOf(OperazioneNonValidaException.class);
        assertThatThrownBy(() -> servizio.partitaDiProva(7, 0, 10, 1)).isInstanceOf(RisorsaNonTrovataException.class);
        assertThatThrownBy(() -> servizio.partitaDiProva(7, -1, 2, 1)).isInstanceOf(RisorsaNonTrovataException.class);
    }
}
