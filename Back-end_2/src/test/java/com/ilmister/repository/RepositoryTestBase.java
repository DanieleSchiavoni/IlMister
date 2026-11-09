package com.ilmister.repository;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.ilmister.entity.Campionato;
import com.ilmister.entity.Carriera;
import com.ilmister.entity.Giocatore;
import com.ilmister.entity.Giornata;
import com.ilmister.entity.Partita;
import com.ilmister.entity.Squadra;
import com.ilmister.entity.Stagione;
import com.ilmister.entity.Utente;
import com.ilmister.entity.enumerazioni.Posizione;

/** Le basi dei test sui repository: database in memoria e metodi per creare in fretta i dati di prova */
@DataJpaTest
@ActiveProfiles("test")
public abstract class RepositoryTestBase {

    @Autowired
    protected TestEntityManager em;

    protected Utente creaUtente(String username) {
        Utente utente = new Utente();
        utente.setUsername(username);
        utente.setEmail(username + "@test.it");
        utente.setPassword("password-di-prova");
        return em.persistAndFlush(utente);
    }

    protected Carriera creaCarriera(Utente utente, String nome) {
        Carriera carriera = new Carriera();
        carriera.setUtente(utente);
        carriera.setNome(nome);
        carriera.setNomeAllenatore("Mister Prova");
        carriera.setDataCorrente(LocalDate.of(2026, 7, 1));
        carriera.setSeme(42L);
        return em.persistAndFlush(carriera);
    }

    protected Campionato creaCampionato(Carriera carriera, String nome, int livello) {
        Campionato campionato = new Campionato();
        campionato.setCarriera(carriera);
        campionato.setNome(nome);
        campionato.setLivello(livello);
        return em.persistAndFlush(campionato);
    }

    protected Squadra creaSquadra(Carriera carriera, Campionato campionato, String nome) {
        Squadra squadra = new Squadra();
        squadra.setCarriera(carriera);
        squadra.setCampionato(campionato);
        squadra.setNome(nome);
        squadra.setNomeBreve(nome);
        squadra.setStadio("Stadio " + nome);
        squadra.setCapienza(5000);
        squadra.setReputazione(50);
        return em.persistAndFlush(squadra);
    }

    /** Un giocatore con tutti gli attributi uguali alla valutazione; senza squadra (svincolato) se squadra è nulla */
    protected Giocatore creaGiocatore(Carriera carriera, Squadra squadra, String cognome, Posizione posizione,
            int valutazione) {
        Giocatore giocatore = new Giocatore();
        giocatore.setCarriera(carriera);
        giocatore.setSquadra(squadra);
        giocatore.setNome("Nome");
        giocatore.setCognome(cognome);
        giocatore.setDataNascita(LocalDate.of(2000, 1, 1));
        giocatore.setPosizione(posizione);
        giocatore.setVelocita(valutazione);
        giocatore.setResistenza(valutazione);
        giocatore.setForza(valutazione);
        giocatore.setTecnica(valutazione);
        giocatore.setPassaggio(valutazione);
        giocatore.setTiro(valutazione);
        giocatore.setDribbling(valutazione);
        giocatore.setContrasto(valutazione);
        giocatore.setIntelligenza(valutazione);
        giocatore.setParate(valutazione);
        giocatore.setValutazione(valutazione);
        giocatore.setPotenziale(valutazione + 5);
        giocatore.setStipendioAnnuo(new BigDecimal("10000.00"));
        giocatore.setScadenzaContratto(LocalDate.of(2028, 6, 30));
        giocatore.setValoreMercato(new BigDecimal("50000.00"));
        return em.persistAndFlush(giocatore);
    }

    protected Stagione creaStagione(Carriera carriera, int annoInizio) {
        Stagione stagione = new Stagione();
        stagione.setCarriera(carriera);
        stagione.setAnnoInizio(annoInizio);
        stagione.setDataInizio(LocalDate.of(annoInizio, 7, 1));
        stagione.setDataFine(LocalDate.of(annoInizio + 1, 6, 30));
        return em.persistAndFlush(stagione);
    }

    protected Giornata creaGiornata(Stagione stagione, Campionato campionato, int numero) {
        Giornata giornata = new Giornata();
        giornata.setStagione(stagione);
        giornata.setCampionato(campionato);
        giornata.setNumero(numero);
        giornata.setDataGiornata(stagione.getDataInizio().plusWeeks(numero));
        return em.persistAndFlush(giornata);
    }

    protected Partita creaPartita(Giornata giornata, Squadra casa, Squadra trasferta) {
        Partita partita = new Partita();
        partita.setGiornata(giornata);
        partita.setSquadraCasa(casa);
        partita.setSquadraTrasferta(trasferta);
        partita.setSeme(7L);
        return em.persistAndFlush(partita);
    }
}
