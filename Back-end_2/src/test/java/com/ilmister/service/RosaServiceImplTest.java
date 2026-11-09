package com.ilmister.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import com.ilmister.dto.GiocatoreResponse;
import com.ilmister.dto.NuovaCarrieraRichiesta;
import com.ilmister.entity.Carriera;
import com.ilmister.entity.Giocatore;
import com.ilmister.entity.Squadra;
import com.ilmister.entity.Utente;
import com.ilmister.exception.RisorsaNonTrovataException;
import com.ilmister.generazione.ConfigurazioneGenerazione;
import com.ilmister.generazione.GeneratoreCampionato;
import com.ilmister.repository.GiocatoreRepository;
import com.ilmister.repository.RepositoryTestBase;
import com.ilmister.repository.SquadraRepository;
import com.ilmister.service.impl.CarrieraServiceImpl;
import com.ilmister.service.impl.RiferimentoTemporale;
import com.ilmister.service.impl.RosaServiceImpl;

@Import({ CarrieraServiceImpl.class, RosaServiceImpl.class, RiferimentoTemporale.class, GeneratoreCampionato.class })
class RosaServiceImplTest extends RepositoryTestBase {

    @TestConfiguration
    static class OrologioFisso {
        @Bean
        Clock clock() {
            return Clock.fixed(Instant.parse("2026-10-31T12:00:00Z"), ZoneOffset.UTC);
        }
    }

    @Autowired
    private CarrieraService carrieraService;
    @Autowired
    private RosaService rosaService;
    @Autowired
    private GeneratoreCampionato generatore;
    @Autowired
    private SquadraRepository squadraRepository;
    @Autowired
    private GiocatoreRepository giocatoreRepository;

    private Utente mario;
    private Carriera carriera;

    @BeforeEach
    void preparaDati() {
        mario = creaUtente("mario");
        String nome = generatore.genera(7, ConfigurazioneGenerazione.predefinita(), LocalDate.of(2026, 7, 1))
                .squadre().get(0).nome();
        carriera = carrieraService.creaCarriera(mario.getId(), new NuovaCarrieraRichiesta("Carriera", "Mister", nome, 7L));
        em.flush();
        em.clear();
    }

    @Test
    void senzaIndicareLaSquadraSiVedeLaRosaDellUtente() {
        List<GiocatoreResponse> rosa = rosaService.rosa(carriera.getId(), mario.getId(), null);

        Squadra dellUtente = squadraRepository.findByCarrieraIdAndAllenataDallUtenteTrue(carriera.getId()).orElseThrow();
        assertThat(rosa).hasSize(26);
        assertThat(rosa.stream().map(GiocatoreResponse::id)).containsExactlyInAnyOrderElementsOf(
                giocatoreRepository.findBySquadraIdOrderByCognomeAscNomeAsc(dellUtente.getId()).stream()
                        .map(Giocatore::getId).toList());
    }

    @Test
    void laRosaEOrdinataPerRepartoEValutazione() {
        List<GiocatoreResponse> rosa = rosaService.rosa(carriera.getId(), mario.getId(), null);

        List<String> reparti = rosa.stream().map(GiocatoreResponse::ruolo).distinct().toList();
        assertThat(reparti).containsExactly("PORTIERE", "DIFENSORE", "CENTROCAMPISTA", "ATTACCANTE");
        for (int i = 1; i < rosa.size(); i++) {
            if (rosa.get(i - 1).ruolo().equals(rosa.get(i).ruolo())) {
                assertThat(rosa.get(i - 1).valutazione()).isGreaterThanOrEqualTo(rosa.get(i).valutazione());
            }
        }
    }

    @Test
    void siPuoVedereLaRosaDiUnaltraSquadraDellaStessaCarriera() {
        Squadra altra = squadraRepository.findByCarrieraId(carriera.getId()).stream()
                .filter(s -> !s.isAllenataDallUtente()).findFirst().orElseThrow();

        List<GiocatoreResponse> rosa = rosaService.rosa(carriera.getId(), mario.getId(), altra.getId());

        assertThat(rosa).hasSize(26);
    }

    @Test
    void ilGiocatoreHaAttributiEdEtaCoerenti() {
        GiocatoreResponse primo = rosaService.rosa(carriera.getId(), mario.getId(), null).get(0);

        GiocatoreResponse dettaglio = rosaService.giocatore(carriera.getId(), mario.getId(), primo.id());

        assertThat(dettaglio).isEqualTo(primo);
        assertThat(dettaglio.eta()).isBetween(15, 45);
        assertThat(dettaglio.attributi()).isNotNull();
        assertThat(dettaglio.disponibile()).isTrue();
        assertThat(dettaglio.infortunato()).isFalse();
    }

    @Test
    void nessunoVedeLeRosePerCarrieraAltrui() {
        Utente luca = creaUtente("luca");
        Giocatore giocatore = giocatoreRepository.findAll().get(0);

        assertThatThrownBy(() -> rosaService.rosa(carriera.getId(), luca.getId(), null))
                .isInstanceOf(RisorsaNonTrovataException.class);
        assertThatThrownBy(() -> rosaService.giocatore(carriera.getId(), luca.getId(), giocatore.getId()))
                .isInstanceOf(RisorsaNonTrovataException.class);
    }

    @Test
    void unaSquadraDiUnaltraCarrieraNonSiTrova() {
        Utente luca = creaUtente("luca");
        Carriera altraCarriera = carrieraService.creaCarriera(luca.getId(),
                new NuovaCarrieraRichiesta("Altra", "Mister", generatore
                        .genera(8, ConfigurazioneGenerazione.predefinita(), LocalDate.of(2026, 7, 1)).squadre().get(0).nome(), 8L));
        em.flush();
        em.clear();
        Squadra straniera = squadraRepository.findByCarrieraId(altraCarriera.getId()).get(0);

        assertThatThrownBy(() -> rosaService.rosa(carriera.getId(), mario.getId(), straniera.getId()))
                .isInstanceOf(RisorsaNonTrovataException.class);
    }
}
