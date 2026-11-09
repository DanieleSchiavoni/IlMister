package com.ilmister.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import com.ilmister.dto.FormazioneRichiesta;
import com.ilmister.dto.FormazioneResponse;
import com.ilmister.dto.GiocatoreResponse;
import com.ilmister.dto.NuovaCarrieraRichiesta;
import com.ilmister.entity.Carriera;
import com.ilmister.entity.Giocatore;
import com.ilmister.entity.Squadra;
import com.ilmister.entity.Utente;
import com.ilmister.entity.enumerazioni.LineaDifensiva;
import com.ilmister.entity.enumerazioni.Mentalita;
import com.ilmister.entity.enumerazioni.Modulo;
import com.ilmister.entity.enumerazioni.Posizione;
import com.ilmister.entity.enumerazioni.Pressing;
import com.ilmister.entity.enumerazioni.Ritmo;
import com.ilmister.exception.OperazioneNonValidaException;
import com.ilmister.exception.RisorsaNonTrovataException;
import com.ilmister.generazione.ConfigurazioneGenerazione;
import com.ilmister.generazione.GeneratoreCampionato;
import com.ilmister.motore.MotorePartita;
import com.ilmister.repository.GiocatoreRepository;
import com.ilmister.repository.RepositoryTestBase;
import com.ilmister.repository.SquadraRepository;
import com.ilmister.service.impl.AvanzamentoServiceImpl;
import com.ilmister.service.impl.CarrieraServiceImpl;
import com.ilmister.service.impl.FormazioneServiceImpl;
import com.ilmister.service.impl.RiferimentoTemporale;

@Import({ CarrieraServiceImpl.class, FormazioneServiceImpl.class, AvanzamentoServiceImpl.class,
        RiferimentoTemporale.class, GeneratoreCampionato.class, MotorePartita.class })
class FormazioneServiceImplTest extends RepositoryTestBase {

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
    private FormazioneService formazioneService;
    @Autowired
    private AvanzamentoService avanzamentoService;
    @Autowired
    private GeneratoreCampionato generatore;
    @Autowired
    private SquadraRepository squadraRepository;
    @Autowired
    private GiocatoreRepository giocatoreRepository;

    private Utente mario;
    private Carriera carriera;
    private Squadra squadra;

    @BeforeEach
    void preparaDati() {
        mario = creaUtente("mario");
        String nome = generatore.genera(7, ConfigurazioneGenerazione.predefinita(), LocalDate.of(2026, 7, 1))
                .squadre().get(0).nome();
        carriera = carrieraService.creaCarriera(mario.getId(), new NuovaCarrieraRichiesta("Carriera", "Mister", nome, 7L));
        em.flush();
        em.clear();
        squadra = squadraRepository.findByCarrieraIdAndAllenataDallUtenteTrue(carriera.getId()).orElseThrow();
    }

    /** Una formazione valida: il portiere migliore e dieci giocatori di movimento, poi sette in panchina */
    private FormazioneRichiesta richiestaValida() {
        List<Giocatore> rosa = giocatoreRepository.findBySquadraIdOrderByCognomeAscNomeAsc(squadra.getId());
        List<Long> portieri = rosa.stream().filter(g -> g.getPosizione() == Posizione.PORTIERE)
                .map(Giocatore::getId).toList();
        List<Long> altri = rosa.stream().filter(g -> g.getPosizione() != Posizione.PORTIERE)
                .map(Giocatore::getId).toList();
        List<Long> titolari = new ArrayList<>();
        titolari.add(portieri.get(0));
        titolari.addAll(altri.subList(0, 10));
        List<Long> panchina = new ArrayList<>(altri.subList(10, 16));
        panchina.add(portieri.get(1));
        return new FormazioneRichiesta(Modulo.M_4_3_3, Mentalita.OFFENSIVA, Pressing.MEDIO, Ritmo.NORMALE,
                LineaDifensiva.MEDIA, titolari, panchina);
    }

    @Test
    void senzaFormazioneSalvataSiProponeQuellaAutomatica() {
        FormazioneResponse proposta = formazioneService.formazione(carriera.getId(), mario.getId());

        assertThat(proposta.salvata()).isFalse();
        assertThat(proposta.modulo()).isEqualTo(Modulo.M_4_3_3);
        assertThat(proposta.titolari()).hasSize(11);
        assertThat(proposta.panchina()).hasSize(7);
        assertThat(proposta.titolari().get(0).siglaPosizione()).isEqualTo("POR");
        assertThat(proposta.titolari()).allMatch(GiocatoreResponse::disponibile);
    }

    @Test
    void siSalvaEIlSalvataggioSiRilegge() {
        FormazioneRichiesta richiesta = richiestaValida();

        FormazioneResponse risposta = formazioneService.salva(carriera.getId(), mario.getId(), richiesta);
        em.flush();
        em.clear();
        FormazioneResponse riletta = formazioneService.formazione(carriera.getId(), mario.getId());

        assertThat(risposta.salvata()).isTrue();
        assertThat(riletta.salvata()).isTrue();
        assertThat(riletta.modulo()).isEqualTo(Modulo.M_4_3_3);
        assertThat(riletta.mentalita()).isEqualTo(Mentalita.OFFENSIVA);
        assertThat(riletta.titolari().stream().map(GiocatoreResponse::id)).containsExactlyElementsOf(richiesta.titolari());
        assertThat(riletta.panchina().stream().map(GiocatoreResponse::id)).containsExactlyElementsOf(richiesta.panchina());
    }

    @Test
    void salvareDiNuovoSostituisceLaFormazione() {
        formazioneService.salva(carriera.getId(), mario.getId(), richiestaValida());
        em.flush();
        em.clear();
        FormazioneRichiesta prima = richiestaValida();
        List<Long> titolari = new ArrayList<>(prima.titolari());
        // Scambio di posto tra due giocatori di movimento
        Long primo = titolari.get(1);
        titolari.set(1, titolari.get(2));
        titolari.set(2, primo);
        FormazioneRichiesta nuova = new FormazioneRichiesta(Modulo.M_4_4_2, Mentalita.DIFENSIVA, Pressing.ALTO,
                Ritmo.VELOCE, LineaDifensiva.ALTA, titolari, prima.panchina());

        formazioneService.salva(carriera.getId(), mario.getId(), nuova);
        em.flush();
        em.clear();
        FormazioneResponse riletta = formazioneService.formazione(carriera.getId(), mario.getId());

        assertThat(riletta.modulo()).isEqualTo(Modulo.M_4_4_2);
        assertThat(riletta.mentalita()).isEqualTo(Mentalita.DIFENSIVA);
        assertThat(riletta.titolari().stream().map(GiocatoreResponse::id)).containsExactlyElementsOf(titolari);
    }

    @Test
    void unGiocatoreNonPuoEssereSchieratoDueVolte() {
        FormazioneRichiesta valida = richiestaValida();
        List<Long> panchina = new ArrayList<>(valida.panchina());
        panchina.set(0, valida.titolari().get(1));
        FormazioneRichiesta doppia = new FormazioneRichiesta(valida.modulo(), valida.mentalita(), valida.pressing(),
                valida.ritmo(), valida.lineaDifensiva(), valida.titolari(), panchina);

        assertThatThrownBy(() -> formazioneService.salva(carriera.getId(), mario.getId(), doppia))
                .isInstanceOf(OperazioneNonValidaException.class).hasMessageContaining("due volte");
    }

    @Test
    void ilPrimoTitolareDeveEssereIlPortiere() {
        FormazioneRichiesta valida = richiestaValida();
        List<Long> titolari = new ArrayList<>(valida.titolari());
        Long portiere = titolari.get(0);
        titolari.set(0, titolari.get(1));
        titolari.set(1, portiere);
        FormazioneRichiesta sbagliata = new FormazioneRichiesta(valida.modulo(), valida.mentalita(), valida.pressing(),
                valida.ritmo(), valida.lineaDifensiva(), titolari, valida.panchina());

        assertThatThrownBy(() -> formazioneService.salva(carriera.getId(), mario.getId(), sbagliata))
                .isInstanceOf(OperazioneNonValidaException.class).hasMessageContaining("portiere");
    }

    @Test
    void nonSiSchieranoGiocatoriDiUnaltraSquadra() {
        Squadra altra = squadraRepository.findByCarrieraId(carriera.getId()).stream()
                .filter(s -> !s.getId().equals(squadra.getId())).findFirst().orElseThrow();
        Long straniero = giocatoreRepository.findBySquadraIdOrderByCognomeAscNomeAsc(altra.getId()).stream()
                .filter(g -> g.getPosizione() != Posizione.PORTIERE).findFirst().orElseThrow().getId();
        FormazioneRichiesta valida = richiestaValida();
        List<Long> titolari = new ArrayList<>(valida.titolari());
        titolari.set(5, straniero);
        FormazioneRichiesta sbagliata = new FormazioneRichiesta(valida.modulo(), valida.mentalita(), valida.pressing(),
                valida.ritmo(), valida.lineaDifensiva(), titolari, valida.panchina());

        assertThatThrownBy(() -> formazioneService.salva(carriera.getId(), mario.getId(), sbagliata))
                .isInstanceOf(OperazioneNonValidaException.class).hasMessageContaining("non è della tua squadra");
    }

    @Test
    void unGiocatoreInfortunatoNonPuoGiocare() {
        FormazioneRichiesta valida = richiestaValida();
        Giocatore infortunato = giocatoreRepository.findById(valida.titolari().get(3)).orElseThrow();
        infortunato.setInfortunatoFinoAl(LocalDate.of(2027, 3, 1));
        infortunato.setTipoInfortunio("Distorsione");
        em.persistAndFlush(infortunato);

        assertThatThrownBy(() -> formazioneService.salva(carriera.getId(), mario.getId(), valida))
                .isInstanceOf(OperazioneNonValidaException.class).hasMessageContaining("non è disponibile");
    }

    @Test
    void nessunoVedeLaFormazioneDiUnAltro() {
        Utente luca = creaUtente("luca");

        assertThatThrownBy(() -> formazioneService.formazione(carriera.getId(), luca.getId()))
                .isInstanceOf(RisorsaNonTrovataException.class);
        assertThatThrownBy(() -> formazioneService.salva(carriera.getId(), luca.getId(), richiestaValida()))
                .isInstanceOf(RisorsaNonTrovataException.class);
    }

    @Test
    void laPartitaDellUtenteSiGiocaConLaFormazioneSalvata() {
        FormazioneRichiesta richiesta = richiestaValida();
        formazioneService.salva(carriera.getId(), mario.getId(), richiesta);
        em.flush();
        em.clear();

        avanzamentoService.avanza(carriera.getId(), mario.getId());
        em.flush();
        em.clear();

        // I titolari salvati sono i soli ad avere giocato dall'inizio nella partita dell'utente
        var titolariInPartita = em.getEntityManager()
                .createQuery("select p.giocatore.id from PrestazioneGiocatore p where p.squadra.id = :s and p.titolare = true", Long.class)
                .setParameter("s", squadra.getId()).getResultList();
        assertThat(titolariInPartita).containsExactlyInAnyOrderElementsOf(richiesta.titolari());
    }
}
