package com.ilmister.service.impl;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ilmister.dto.NuovaCarrieraRichiesta;
import com.ilmister.entity.Campionato;
import com.ilmister.entity.Carriera;
import com.ilmister.entity.Giocatore;
import com.ilmister.entity.Squadra;
import com.ilmister.entity.Stagione;
import com.ilmister.entity.Utente;
import com.ilmister.exception.OperazioneNonValidaException;
import com.ilmister.exception.RisorsaNonTrovataException;
import com.ilmister.generazione.Attributi;
import com.ilmister.generazione.CampionatoGenerato;
import com.ilmister.generazione.ConfigurazioneGenerazione;
import com.ilmister.generazione.GeneratoreCampionato;
import com.ilmister.generazione.GiocatoreGenerato;
import com.ilmister.generazione.SquadraGenerata;
import com.ilmister.repository.CampionatoRepository;
import com.ilmister.repository.CarrieraRepository;
import com.ilmister.repository.GiocatoreRepository;
import com.ilmister.repository.SquadraRepository;
import com.ilmister.repository.StagioneRepository;
import com.ilmister.repository.UtenteRepository;
import com.ilmister.service.CarrieraService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CarrieraServiceImpl implements CarrieraService {

    static final int MASSIMO_CARRIERE_PER_UTENTE = 5;

    private final UtenteRepository utenteRepository;
    private final CarrieraRepository carrieraRepository;
    private final StagioneRepository stagioneRepository;
    private final CampionatoRepository campionatoRepository;
    private final SquadraRepository squadraRepository;
    private final GiocatoreRepository giocatoreRepository;
    private final GeneratoreCampionato generatore;
    private final Clock clock;

    /** Il gioco inizia il primo luglio dell'anno in corso */
    static LocalDate inizioStagione(Clock clock) {
        return LocalDate.of(LocalDate.now(clock).getYear(), 7, 1);
    }

    @Override
    @Transactional
    public Carriera creaCarriera(Long utenteId, NuovaCarrieraRichiesta richiesta) {
        Utente utente = utenteRepository.findById(utenteId)
                .orElseThrow(() -> new RisorsaNonTrovataException("Utente non trovato"));
        if (carrieraRepository.countByUtenteId(utenteId) >= MASSIMO_CARRIERE_PER_UTENTE) {
            throw new OperazioneNonValidaException("Hai raggiunto il massimo di " + MASSIMO_CARRIERE_PER_UTENTE
                    + " carriere: eliminane una per iniziarne un'altra");
        }

        LocalDate inizio = inizioStagione(clock);
        CampionatoGenerato dati = generatore.genera(richiesta.seme(), ConfigurazioneGenerazione.predefinita(), inizio);
        String nomeScelto = richiesta.nomeSquadra().trim();
        SquadraGenerata sceltaDati = dati.squadre().stream()
                .filter(squadra -> squadra.nome().equalsIgnoreCase(nomeScelto))
                .findFirst()
                .orElseThrow(() -> new OperazioneNonValidaException(
                        "La squadra \"" + nomeScelto + "\" non esiste in questo campionato"));

        Carriera carriera = new Carriera();
        carriera.setUtente(utente);
        carriera.setNome(richiesta.nome().trim());
        carriera.setNomeAllenatore(richiesta.nomeAllenatore().trim());
        carriera.setDataCorrente(inizio);
        carriera.setSeme(richiesta.seme());
        carrieraRepository.save(carriera);

        Stagione stagione = new Stagione();
        stagione.setCarriera(carriera);
        stagione.setAnnoInizio(inizio.getYear());
        stagione.setDataInizio(inizio);
        stagione.setDataFine(LocalDate.of(inizio.getYear() + 1, 6, 30));
        stagioneRepository.save(stagione);

        Campionato campionato = new Campionato();
        campionato.setCarriera(carriera);
        campionato.setNome(dati.nome());
        campionato.setLivello(dati.livello());
        campionatoRepository.save(campionato);

        for (SquadraGenerata squadraDati : dati.squadre()) {
            Squadra squadra = aSquadra(squadraDati, carriera, campionato);
            squadra.setAllenataDallUtente(squadraDati == sceltaDati);
            squadraRepository.save(squadra);
            giocatoreRepository.saveAll(squadraDati.giocatori().stream()
                    .map(giocatoreDati -> aGiocatore(giocatoreDati, carriera, squadra))
                    .toList());
        }
        return carriera;
    }

    private Squadra aSquadra(SquadraGenerata dati, Carriera carriera, Campionato campionato) {
        Squadra squadra = new Squadra();
        squadra.setCarriera(carriera);
        squadra.setCampionato(campionato);
        squadra.setNome(dati.nome());
        squadra.setNomeBreve(dati.nomeBreve());
        squadra.setCitta(dati.citta());
        squadra.setStadio(dati.stadio());
        squadra.setCapienza(dati.capienza());
        squadra.setReputazione(dati.reputazione());
        squadra.setColorePrimario(dati.colorePrimario());
        squadra.setColoreSecondario(dati.coloreSecondario());
        squadra.setBudgetTrasferimenti(dati.budgetTrasferimenti());
        return squadra;
    }

    private Giocatore aGiocatore(GiocatoreGenerato dati, Carriera carriera, Squadra squadra) {
        Attributi attributi = dati.attributi();
        Giocatore giocatore = new Giocatore();
        giocatore.setCarriera(carriera);
        giocatore.setSquadra(squadra);
        giocatore.setNome(dati.nome());
        giocatore.setCognome(dati.cognome());
        giocatore.setDataNascita(dati.dataNascita());
        giocatore.setPosizione(dati.posizione());
        giocatore.setPiede(dati.piede());
        giocatore.setNumeroMaglia(dati.numeroMaglia());
        giocatore.setVelocita(attributi.velocita());
        giocatore.setResistenza(attributi.resistenza());
        giocatore.setForza(attributi.forza());
        giocatore.setTecnica(attributi.tecnica());
        giocatore.setPassaggio(attributi.passaggio());
        giocatore.setTiro(attributi.tiro());
        giocatore.setDribbling(attributi.dribbling());
        giocatore.setContrasto(attributi.contrasto());
        giocatore.setIntelligenza(attributi.intelligenza());
        giocatore.setParate(attributi.parate());
        giocatore.setValutazione(dati.valutazione());
        giocatore.setPotenziale(dati.potenziale());
        giocatore.setStipendioAnnuo(dati.stipendioAnnuo());
        giocatore.setScadenzaContratto(dati.scadenzaContratto());
        giocatore.setValoreMercato(dati.valoreMercato());
        return giocatore;
    }
}
