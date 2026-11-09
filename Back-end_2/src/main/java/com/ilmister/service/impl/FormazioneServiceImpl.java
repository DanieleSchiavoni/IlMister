package com.ilmister.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ilmister.dto.FormazioneRichiesta;
import com.ilmister.dto.FormazioneResponse;
import com.ilmister.dto.GiocatoreResponse;
import com.ilmister.entity.Carriera;
import com.ilmister.entity.Formazione;
import com.ilmister.entity.FormazioneGiocatore;
import com.ilmister.entity.Giocatore;
import com.ilmister.entity.Squadra;
import com.ilmister.entity.enumerazioni.Modulo;
import com.ilmister.entity.enumerazioni.Posizione;
import com.ilmister.exception.OperazioneNonValidaException;
import com.ilmister.exception.RisorsaNonTrovataException;
import com.ilmister.motore.FormazioneAutomatica;
import com.ilmister.motore.Istruzioni;
import com.ilmister.repository.CarrieraRepository;
import com.ilmister.repository.FormazioneGiocatoreRepository;
import com.ilmister.repository.FormazioneRepository;
import com.ilmister.repository.GiocatoreRepository;
import com.ilmister.repository.SquadraRepository;
import com.ilmister.service.FormazioneService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FormazioneServiceImpl implements FormazioneService {

    static final Modulo MODULO_PREDEFINITO = Modulo.M_4_3_3;

    private final CarrieraRepository carrieraRepository;
    private final SquadraRepository squadraRepository;
    private final GiocatoreRepository giocatoreRepository;
    private final FormazioneRepository formazioneRepository;
    private final FormazioneGiocatoreRepository formazioneGiocatoreRepository;
    private final RiferimentoTemporale riferimentoTemporale;

    @Override
    @Transactional(readOnly = true)
    public FormazioneResponse formazione(Long carrieraId, Long utenteId) {
        Carriera carriera = trovaCarriera(carrieraId, utenteId);
        Squadra squadra = trovaSquadra(carrieraId);
        LocalDate data = riferimentoTemporale.dataProssimaGiornata(carriera);

        Optional<Formazione> salvata = formazioneRepository.findBySquadraId(squadra.getId());
        if (salvata.isPresent()) {
            Formazione formazione = salvata.get();
            List<FormazioneGiocatore> posti = formazioneGiocatoreRepository
                    .findByFormazioneIdOrderByPostoAsc(formazione.getId());
            return new FormazioneResponse(true, formazione.getModulo(), formazione.getMentalita(),
                    formazione.getPressing(), formazione.getRitmo(), formazione.getLineaDifensiva(),
                    risposte(posti.stream().filter(FormazioneGiocatore::titolare).map(FormazioneGiocatore::getGiocatore).toList(), data),
                    risposte(posti.stream().filter(posto -> !posto.titolare()).map(FormazioneGiocatore::getGiocatore).toList(), data));
        }

        // Nessuna formazione salvata: la proposta automatica
        List<Giocatore> rosa = giocatoreRepository.findBySquadraIdOrderByCognomeAscNomeAsc(squadra.getId());
        List<Giocatore> disponibili = rosa.stream().filter(giocatore -> giocatore.disponibileAl(data)).toList();
        if (disponibili.size() < Modulo.TITOLARI) {
            disponibili = rosa;
        }
        if (disponibili.size() < Modulo.TITOLARI) {
            throw new OperazioneNonValidaException("La squadra ha meno di " + Modulo.TITOLARI + " giocatori");
        }
        Map<Long, Giocatore> perId = new HashMap<>();
        disponibili.forEach(giocatore -> perId.put(giocatore.getId(), giocatore));
        FormazioneAutomatica.Scelta scelta = FormazioneAutomatica.scegli(
                disponibili.stream().map(giocatore -> GiocatoreMapper.inCampo(giocatore, giocatore.getEnergia())).toList(),
                MODULO_PREDEFINITO);
        Istruzioni istruzioni = Istruzioni.equilibrate();
        return new FormazioneResponse(false, MODULO_PREDEFINITO, istruzioni.mentalita(), istruzioni.pressing(),
                istruzioni.ritmo(), istruzioni.lineaDifensiva(),
                risposte(scelta.titolari().stream().map(g -> perId.get(g.id())).toList(), data),
                risposte(scelta.panchina().stream().map(g -> perId.get(g.id())).toList(), data));
    }

    @Override
    @Transactional
    public FormazioneResponse salva(Long carrieraId, Long utenteId, FormazioneRichiesta richiesta) {
        Carriera carriera = trovaCarriera(carrieraId, utenteId);
        Squadra squadra = trovaSquadra(carrieraId);
        LocalDate data = riferimentoTemporale.dataProssimaGiornata(carriera);

        List<Long> schierati = new ArrayList<>(richiesta.titolari());
        schierati.addAll(richiesta.panchina());
        if (new HashSet<>(schierati).size() != schierati.size()) {
            throw new OperazioneNonValidaException("Un giocatore non può essere schierato due volte");
        }
        Map<Long, Giocatore> rosa = new HashMap<>();
        giocatoreRepository.findBySquadraIdOrderByCognomeAscNomeAsc(squadra.getId())
                .forEach(giocatore -> rosa.put(giocatore.getId(), giocatore));
        for (Long id : schierati) {
            Giocatore giocatore = rosa.get(id);
            if (giocatore == null) {
                throw new OperazioneNonValidaException("Il giocatore " + id + " non è della tua squadra");
            }
            if (!giocatore.disponibileAl(data)) {
                throw new OperazioneNonValidaException(
                        giocatore.nomeCompleto() + " non è disponibile (infortunato o squalificato)");
            }
        }
        if (rosa.get(richiesta.titolari().get(0)).getPosizione() != Posizione.PORTIERE) {
            throw new OperazioneNonValidaException("Il primo titolare deve essere il portiere");
        }
        for (Long id : richiesta.titolari().subList(1, Modulo.TITOLARI)) {
            if (rosa.get(id).getPosizione() == Posizione.PORTIERE) {
                throw new OperazioneNonValidaException("In campo può giocare un solo portiere");
            }
        }

        Formazione formazione = formazioneRepository.findBySquadraId(squadra.getId()).orElseGet(() -> {
            Formazione nuova = new Formazione();
            nuova.setSquadra(squadra);
            return nuova;
        });
        formazione.setModulo(richiesta.modulo());
        formazione.setMentalita(richiesta.mentalita());
        formazione.setPressing(richiesta.pressing());
        formazione.setRitmo(richiesta.ritmo());
        formazione.setLineaDifensiva(richiesta.lineaDifensiva());
        formazione = formazioneRepository.saveAndFlush(formazione);
        Long formazioneId = formazione.getId();

        // Si svuota la formazione con una query e poi si riscrivono i posti (vedi il repository)
        formazioneGiocatoreRepository.eliminaPerFormazione(formazioneId);
        List<FormazioneGiocatore> righe = new ArrayList<>();
        for (int posto = 0; posto < Modulo.TITOLARI; posto++) {
            righe.add(riga(formazioneId, richiesta.titolari().get(posto), posto));
        }
        for (int i = 0; i < richiesta.panchina().size(); i++) {
            righe.add(riga(formazioneId, richiesta.panchina().get(i), FormazioneGiocatore.PRIMO_POSTO_PANCHINA + i));
        }
        formazioneGiocatoreRepository.saveAll(righe);
        formazioneGiocatoreRepository.flush();

        return formazione(carrieraId, utenteId);
    }

    private FormazioneGiocatore riga(Long formazioneId, Long giocatoreId, int posto) {
        FormazioneGiocatore riga = new FormazioneGiocatore();
        riga.setFormazione(formazioneRepository.getReferenceById(formazioneId));
        riga.setGiocatore(giocatoreRepository.getReferenceById(giocatoreId));
        riga.setPosto(posto);
        return riga;
    }

    private List<GiocatoreResponse> risposte(List<Giocatore> giocatori, LocalDate data) {
        return giocatori.stream().map(giocatore -> GiocatoreMapper.aResponse(giocatore, data)).toList();
    }

    private Carriera trovaCarriera(Long carrieraId, Long utenteId) {
        return carrieraRepository.findByIdAndUtenteId(carrieraId, utenteId)
                .orElseThrow(() -> new RisorsaNonTrovataException("Carriera non trovata"));
    }

    private Squadra trovaSquadra(Long carrieraId) {
        return squadraRepository.findByCarrieraIdAndAllenataDallUtenteTrue(carrieraId)
                .orElseThrow(() -> new RisorsaNonTrovataException("Squadra dell'utente non trovata"));
    }
}
