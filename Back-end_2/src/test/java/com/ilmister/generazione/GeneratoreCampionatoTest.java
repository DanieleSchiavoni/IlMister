package com.ilmister.generazione;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.ilmister.entity.enumerazioni.Modulo;
import com.ilmister.entity.enumerazioni.Posizione;

class GeneratoreCampionatoTest {

    private static final LocalDate INIZIO = LocalDate.of(2026, 7, 1);

    private final GeneratoreCampionato generatore = new GeneratoreCampionato();

    private CampionatoGenerato genera(long seme) {
        return generatore.genera(seme, ConfigurazioneGenerazione.predefinita(), INIZIO);
    }

    @Test
    void ilCampionatoHaIlNumeroDiSquadreRichiesto() {
        CampionatoGenerato campionato = generatore.genera(5, new ConfigurazioneGenerazione("Prova", 16, 60), INIZIO);

        assertThat(campionato.squadre()).hasSize(16);
        assertThat(campionato.livello()).isEqualTo(1);
        assertThat(campionato.seme()).isEqualTo(5);
    }

    @Test
    void lostessoSemeGeneraLoStessoCampionato() {
        assertThat(genera(42)).isEqualTo(genera(42));
        assertThat(genera(42)).isNotEqualTo(genera(43));
    }

    @Test
    void leSquadreHannoNomiECittaTuttiDiversi() {
        for (long seme = 1; seme <= 30; seme++) {
            List<SquadraGenerata> squadre = genera(seme).squadre();

            assertThat(squadre.stream().map(SquadraGenerata::nome).collect(Collectors.toSet())).hasSize(squadre.size());
            assertThat(squadre.stream().map(SquadraGenerata::citta).collect(Collectors.toSet())).hasSize(squadre.size());
        }
    }

    @Test
    void ogniRosaHaLaComposizioneStabilita() {
        for (SquadraGenerata squadra : genera(7).squadre()) {
            assertThat(squadra.giocatori()).hasSize(ConfigurazioneGenerazione.GIOCATORI_PER_SQUADRA);
            Map<Posizione, Long> conteggio = squadra.giocatori().stream()
                    .collect(Collectors.groupingBy(GiocatoreGenerato::posizione, Collectors.counting()));
            GeneratoreCampionato.COMPOSIZIONE_ROSA.forEach((posizione, quanti) ->
                    assertThat(conteggio.get(posizione)).as(squadra.nome() + " " + posizione).isEqualTo((long) quanti));
        }
    }

    @Test
    void ogniRosaPuoSchierareQualsiasiModulo() {
        for (SquadraGenerata squadra : genera(9).squadre()) {
            Map<Posizione, Long> disponibili = squadra.giocatori().stream()
                    .collect(Collectors.groupingBy(GiocatoreGenerato::posizione, Collectors.counting()));
            for (Modulo modulo : Modulo.values()) {
                Map<Posizione, Long> richiesti = modulo.getPosti().stream()
                        .collect(Collectors.groupingBy(posizione -> posizione, Collectors.counting()));
                richiesti.forEach((posizione, quanti) -> assertThat(disponibili.getOrDefault(posizione, 0L))
                        .as(modulo.getCodice() + " " + posizione).isGreaterThanOrEqualTo(quanti));
            }
        }
    }

    @Test
    void iNumeriDiMagliaSonoUniciInOgniSquadra() {
        for (SquadraGenerata squadra : genera(3).squadre()) {
            Set<Integer> numeri = new HashSet<>();
            for (GiocatoreGenerato giocatore : squadra.giocatori()) {
                assertThat(giocatore.numeroMaglia()).isBetween(1, 99);
                assertThat(numeri.add(giocatore.numeroMaglia())).as(squadra.nome() + " numero " + giocatore.numeroMaglia()).isTrue();
            }
        }
    }

    @Test
    void iCognomiDeiGiocatoriDiUnaSquadraSonoTuttiDiversi() {
        for (long seme = 1; seme <= 20; seme++) {
            for (SquadraGenerata squadra : genera(seme).squadre()) {
                Set<String> cognomi = squadra.giocatori().stream().map(GiocatoreGenerato::cognome)
                        .collect(Collectors.toSet());

                assertThat(cognomi).as(squadra.nome()).hasSize(squadra.giocatori().size());
            }
        }
    }

    @Test
    void etaPotenzialeEContrattoSonoCoerenti() {
        for (long seme = 1; seme <= 10; seme++) {
            for (SquadraGenerata squadra : genera(seme).squadre()) {
                for (GiocatoreGenerato giocatore : squadra.giocatori()) {
                    int eta = Period.between(giocatore.dataNascita(), INIZIO).getYears();
                    assertThat(eta).isBetween(17, 38);
                    assertThat(giocatore.potenziale()).isGreaterThanOrEqualTo(giocatore.valutazione());
                    assertThat(giocatore.potenziale()).isLessThanOrEqualTo(CalcolatoreAttributi.MASSIMO);
                    assertThat(giocatore.scadenzaContratto()).isAfter(INIZIO);
                    assertThat(giocatore.valoreMercato().signum()).isPositive();
                    assertThat(giocatore.stipendioAnnuo().signum()).isPositive();
                    // La valutazione salvata è sempre quella che si ricava dagli attributi
                    assertThat(giocatore.valutazione())
                            .isEqualTo(CalcolatoreAttributi.valutazione(giocatore.posizione(), giocatore.attributi()));
                }
            }
        }
    }

    @Test
    void chiHaPiuReputazioneHaInMediaUnaSquadraPiuForte() {
        double sommaDifferenze = 0;
        for (long seme = 1; seme <= 20; seme++) {
            List<SquadraGenerata> squadre = genera(seme).squadre();
            SquadraGenerata piuForte = squadre.stream().max((a, b) -> Integer.compare(a.reputazione(), b.reputazione())).orElseThrow();
            SquadraGenerata piuDebole = squadre.stream().min((a, b) -> Integer.compare(a.reputazione(), b.reputazione())).orElseThrow();
            sommaDifferenze += piuForte.forzaMedia() - piuDebole.forzaMedia();
        }

        // La squadra di maggior reputazione è in media molto più forte di quella di minor reputazione
        assertThat(sommaDifferenze / 20).isGreaterThan(8.0);
    }

    @Test
    void ilLivelloMedioDelCampionatoSiAvvicinaAQuelloRichiesto() {
        double somma = 0;
        int squadreTotali = 0;
        for (long seme = 1; seme <= 20; seme++) {
            for (SquadraGenerata squadra : genera(seme).squadre()) {
                somma += squadra.forzaMedia();
                squadreTotali++;
            }
        }

        assertThat(somma / squadreTotali).isBetween(55.0, 65.0);
    }

    @Test
    void iParametriNonValidiSonoRifiutati() {
        assertThatThrownBy(() -> new ConfigurazioneGenerazione("X", 9, 58)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ConfigurazioneGenerazione("X", 2, 58)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ConfigurazioneGenerazione("X", 10, 99)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void gliElenchiDeiNomiNonHannoDoppioni() {
        assertThat(new HashSet<>(NomiItaliani.NOMI)).hasSize(NomiItaliani.NOMI.size());
        assertThat(new HashSet<>(NomiItaliani.COGNOMI)).hasSize(NomiItaliani.COGNOMI.size());
        assertThat(new HashSet<>(NomiItaliani.CITTA)).hasSize(NomiItaliani.CITTA.size());
        assertThat(Arrays.stream(Posizione.values()).count()).isEqualTo(GeneratoreCampionato.COMPOSIZIONE_ROSA.size());
    }
}
