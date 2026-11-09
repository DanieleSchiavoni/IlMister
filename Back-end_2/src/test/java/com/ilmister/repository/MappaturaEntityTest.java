package com.ilmister.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ilmister.entity.Campionato;
import com.ilmister.entity.Carriera;
import com.ilmister.entity.EventoPartita;
import com.ilmister.entity.Formazione;
import com.ilmister.entity.FormazioneGiocatore;
import com.ilmister.entity.Giocatore;
import com.ilmister.entity.Giornata;
import com.ilmister.entity.Partita;
import com.ilmister.entity.PrestazioneGiocatore;
import com.ilmister.entity.Squadra;
import com.ilmister.entity.Stagione;
import com.ilmister.entity.StatisticaPartita;
import com.ilmister.entity.Utente;
import com.ilmister.entity.VoceClassifica;
import com.ilmister.entity.enumerazioni.Modulo;
import com.ilmister.entity.enumerazioni.Posizione;
import com.ilmister.entity.enumerazioni.StatoPartita;
import com.ilmister.entity.enumerazioni.TipoEvento;

/**
 * Salva una carriera completa e la rilegge: verifica che ogni entity corrisponda alle tabelle
 * (tipi, enum, testi lunghi) e che le cancellazioni a cascata funzionino come in MySQL.
 */
class MappaturaEntityTest extends RepositoryTestBase {

    @Autowired
    private CarrieraRepository carrieraRepository;
    @Autowired
    private GiocatoreRepository giocatoreRepository;
    @Autowired
    private SquadraRepository squadraRepository;
    @Autowired
    private PartitaRepository partitaRepository;
    @Autowired
    private EventoPartitaRepository eventoPartitaRepository;

    private Utente utente;
    private Carriera carriera;
    private Partita partita;
    private Giocatore attaccante;
    private String statoLungo;

    @BeforeEach
    void creaCarrieraCompleta() {
        utente = creaUtente("mario");
        carriera = creaCarriera(utente, "Carriera completa");
        Campionato campionato = creaCampionato(carriera, "Campionato", 1);
        Squadra casa = creaSquadra(carriera, campionato, "Casa");
        Squadra trasferta = creaSquadra(carriera, campionato, "Trasferta");
        attaccante = creaGiocatore(carriera, casa, "Bomber", Posizione.ATTACCANTE, 75);
        Giocatore portiere = creaGiocatore(carriera, trasferta, "Portiere", Posizione.PORTIERE, 65);
        Stagione stagione = creaStagione(carriera, 2026);
        Giornata giornata = creaGiornata(stagione, campionato, 1);
        partita = creaPartita(giornata, casa, trasferta);

        // Lo stato salvato all'intervallo è un testo molto più lungo dei 255 caratteri predefiniti
        statoLungo = "{\"minuto\":45,\"note\":\"" + "x".repeat(5000) + "\"}";
        partita.setStato(StatoPartita.INTERVALLO);
        partita.setGolCasa(1);
        partita.setGolTrasferta(0);
        partita.setStatoSimulazione(statoLungo);
        em.persistAndFlush(partita);

        EventoPartita gol = new EventoPartita();
        gol.setPartita(partita);
        gol.setOrdine(1);
        gol.setMinuto(23);
        gol.setTipo(TipoEvento.GOL);
        gol.setSquadra(casa);
        gol.setGiocatore(attaccante);
        gol.setXg(new BigDecimal("0.34"));
        gol.setTesto("Gol! Bomber batte il portiere con un tiro angolato.");
        em.persistAndFlush(gol);

        StatisticaPartita statistica = new StatisticaPartita();
        statistica.setPartita(partita);
        statistica.setSquadra(casa);
        statistica.setPossesso(54);
        statistica.setTiri(7);
        statistica.setTiriInPorta(3);
        statistica.setXg(new BigDecimal("1.45"));
        em.persistAndFlush(statistica);

        PrestazioneGiocatore prestazione = new PrestazioneGiocatore();
        prestazione.setPartita(partita);
        prestazione.setGiocatore(attaccante);
        prestazione.setSquadra(casa);
        prestazione.setTitolare(true);
        prestazione.setMinutiGiocati(45);
        prestazione.setGol(1);
        prestazione.setVoto(new BigDecimal("7.5"));
        em.persistAndFlush(prestazione);

        VoceClassifica voce = new VoceClassifica();
        voce.setStagione(stagione);
        voce.setCampionato(campionato);
        voce.setSquadra(casa);
        em.persistAndFlush(voce);

        Formazione formazione = new Formazione();
        formazione.setSquadra(casa);
        formazione.setModulo(Modulo.M_4_2_3_1);
        em.persistAndFlush(formazione);
        FormazioneGiocatore schierato = new FormazioneGiocatore();
        schierato.setFormazione(formazione);
        schierato.setGiocatore(attaccante);
        schierato.setPosto(10);
        em.persistAndFlush(schierato);

        em.clear();
    }

    @Test
    void iTestiLunghiEGliEnumSiRileggonoIdentici() {
        Partita riletta = partitaRepository.findById(partita.getId()).orElseThrow();

        assertThat(riletta.getStato()).isEqualTo(StatoPartita.INTERVALLO);
        assertThat(riletta.getStatoSimulazione()).isEqualTo(statoLungo);
        assertThat(riletta.getGolCasa()).isEqualTo(1);
        assertThat(riletta.getSeme()).isEqualTo(7L);

        EventoPartita evento = eventoPartitaRepository.findByPartitaIdOrderByOrdineAsc(partita.getId()).get(0);
        assertThat(evento.getTipo()).isEqualTo(TipoEvento.GOL);
        assertThat(evento.getXg()).isEqualByComparingTo("0.34");
        assertThat(evento.getSecondoGiocatore()).isNull();
    }

    @Test
    void laVersioneDellaCarrieraAumentaAdOgniModifica() {
        Carriera salvata = carrieraRepository.findById(carriera.getId()).orElseThrow();
        assertThat(salvata.getVersione()).isZero();
        assertThat(salvata.getDataCreazione()).isNotNull();

        salvata.setFiduciaPresidente(75);
        carrieraRepository.saveAndFlush(salvata);

        assertThat(salvata.getVersione()).isEqualTo(1L);
        assertThat(salvata.getDataAggiornamento()).isAfterOrEqualTo(salvata.getDataCreazione());
    }

    @Test
    void eliminandoLaCarrieraSparisceTuttoCioCheLeApparteneva() {
        em.getEntityManager().createQuery("delete from Carriera c where c.id = :id")
                .setParameter("id", carriera.getId()).executeUpdate();
        em.clear();

        assertThat(carrieraRepository.count()).isZero();
        assertThat(squadraRepository.count()).isZero();
        assertThat(giocatoreRepository.count()).isZero();
        assertThat(partitaRepository.count()).isZero();
        assertThat(eventoPartitaRepository.count()).isZero();
        assertThat(em.getEntityManager().createQuery("select count(x) from VoceClassifica x", Long.class)
                .getSingleResult()).isZero();
        assertThat(em.getEntityManager().createQuery("select count(x) from FormazioneGiocatore x", Long.class)
                .getSingleResult()).isZero();
        // L'utente invece resta
        assertThat(em.find(Utente.class, utente.getId())).isNotNull();
    }

    @Test
    void eliminandoLUtenteSparisconoLeSueCarriere() {
        em.getEntityManager().createQuery("delete from Utente u where u.id = :id")
                .setParameter("id", utente.getId()).executeUpdate();
        em.clear();

        assertThat(carrieraRepository.count()).isZero();
        assertThat(giocatoreRepository.count()).isZero();
        assertThat(partitaRepository.count()).isZero();
    }
}
