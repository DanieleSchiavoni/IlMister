package com.ilmister.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ilmister.dto.DecisioniIntervalloRichiesta;
import com.ilmister.dto.GiornataGiocataResponse;
import com.ilmister.dto.PrimoTempoResponse;
import com.ilmister.dto.RisultatoPartitaResponse;
import com.ilmister.dto.SituazioneGiocatoreResponse;
import com.ilmister.entity.Campionato;
import com.ilmister.entity.Carriera;
import com.ilmister.entity.EventoPartita;
import com.ilmister.entity.Formazione;
import com.ilmister.entity.FormazioneGiocatore;
import com.ilmister.entity.Giocatore;
import com.ilmister.entity.Giornata;
import com.ilmister.entity.MovimentoFinanziario;
import com.ilmister.entity.Partita;
import com.ilmister.entity.PrestazioneGiocatore;
import com.ilmister.entity.Squadra;
import com.ilmister.entity.Stagione;
import com.ilmister.entity.StatisticaPartita;
import com.ilmister.entity.VoceClassifica;
import com.ilmister.entity.enumerazioni.Modulo;
import com.ilmister.entity.enumerazioni.StatoPartita;
import com.ilmister.generazione.GeneratoreCampionato;
import com.ilmister.generazione.Attributi;
import com.ilmister.entity.enumerazioni.TipoMovimento;
import com.ilmister.entity.enumerazioni.StatoStagione;
import com.ilmister.exception.OperazioneNonValidaException;
import com.ilmister.exception.RisorsaNonTrovataException;
import com.ilmister.motore.DatiPartita;
import com.ilmister.motore.DecisioniIntervallo;
import com.ilmister.motore.EventoSimulato;
import com.ilmister.motore.FormazioneAutomatica;
import com.ilmister.motore.GiocatoreInCampo;
import com.ilmister.motore.InfortunioSimulato;
import com.ilmister.motore.Istruzioni;
import com.ilmister.motore.Lato;
import com.ilmister.motore.MotorePartita;
import com.ilmister.motore.PrestazioneSimulata;
import com.ilmister.motore.RisultatoPartita;
import com.ilmister.motore.RisultatoPrimoTempo;
import com.ilmister.motore.SituazioneGiocatore;
import com.ilmister.motore.Sostituzione;
import com.ilmister.motore.SquadraInCampo;
import com.ilmister.motore.StatisticheSquadra;
import com.ilmister.repository.CarrieraRepository;
import com.ilmister.repository.ClassificaRepository;
import com.ilmister.repository.EventoPartitaRepository;
import com.ilmister.repository.FormazioneGiocatoreRepository;
import com.ilmister.repository.FormazioneRepository;
import com.ilmister.repository.GiocatoreRepository;
import com.ilmister.repository.GiornataRepository;
import com.ilmister.repository.MovimentoFinanziarioRepository;
import com.ilmister.repository.PartitaRepository;
import com.ilmister.repository.PrestazioneGiocatoreRepository;
import com.ilmister.repository.SquadraRepository;
import com.ilmister.repository.StagioneRepository;
import com.ilmister.repository.StatisticaPartitaRepository;
import com.ilmister.service.AvanzamentoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AvanzamentoServiceImpl implements AvanzamentoService {

    /** Il modulo di chi non ha una formazione salvata: tutte le squadre del computer */
    private static final Modulo MODULO_PREDEFINITO = Modulo.M_4_3_3;

    private final CarrieraRepository carrieraRepository;
    private final StagioneRepository stagioneRepository;
    private final SquadraRepository squadraRepository;
    private final GiocatoreRepository giocatoreRepository;
    private final GiornataRepository giornataRepository;
    private final PartitaRepository partitaRepository;
    private final ClassificaRepository classificaRepository;
    private final EventoPartitaRepository eventoPartitaRepository;
    private final StatisticaPartitaRepository statisticaPartitaRepository;
    private final PrestazioneGiocatoreRepository prestazioneGiocatoreRepository;
    private final FormazioneRepository formazioneRepository;
    private final FormazioneGiocatoreRepository formazioneGiocatoreRepository;
    private final MovimentoFinanziarioRepository movimentoRepository;
    private final MotorePartita motore;

    /** Tutto ciò che serve per giocare la prossima giornata */
    private record Contesto(Carriera carriera, Stagione stagione, Squadra dellUtente, Campionato campionato,
            Giornata giornata, List<Partita> partite) {

        Partita partitaDellUtente() {
            return partite.stream()
                    .filter(partita -> partita.getSquadraCasa().getId().equals(dellUtente.getId())
                            || partita.getSquadraTrasferta().getId().equals(dellUtente.getId()))
                    .findFirst()
                    .orElseThrow(() -> new RisorsaNonTrovataException("Partita dell'utente non trovata"));
        }
    }

    private Contesto carica(Long carrieraId, Long utenteId) {
        Carriera carriera = carrieraRepository.findByIdAndUtenteId(carrieraId, utenteId)
                .orElseThrow(() -> new RisorsaNonTrovataException("Carriera non trovata"));
        Stagione stagione = stagioneRepository.findByCarrieraIdAndStato(carrieraId, StatoStagione.IN_CORSO)
                .orElseThrow(() -> new OperazioneNonValidaException("La stagione è conclusa"));
        Squadra dellUtente = squadraRepository.findByCarrieraIdAndAllenataDallUtenteTrue(carrieraId)
                .orElseThrow(() -> new RisorsaNonTrovataException("Squadra dell'utente non trovata"));
        Campionato campionato = dellUtente.getCampionato();

        int numero = partitaRepository.prossimaGiornata(stagione.getId(), campionato.getId(), StatoPartita.CONCLUSA)
                .orElseThrow(() -> new OperazioneNonValidaException("Il campionato è finito"));
        Giornata giornata = giornataRepository
                .findByStagioneIdAndCampionatoIdAndNumero(stagione.getId(), campionato.getId(), numero)
                .orElseThrow(() -> new RisorsaNonTrovataException("Giornata non trovata"));
        List<Partita> partite = partitaRepository.findByGiornataId(giornata.getId());
        return new Contesto(carriera, stagione, dellUtente, campionato, giornata, partite);
    }

    @Override
    @Transactional
    public GiornataGiocataResponse avanza(Long carrieraId, Long utenteId) {
        return giocaGiornata(carrieraId, utenteId, null);
    }

    @Override
    @Transactional
    public GiornataGiocataResponse concludiPartita(Long carrieraId, Long utenteId,
            DecisioniIntervalloRichiesta decisioni) {
        return giocaGiornata(carrieraId, utenteId,
                decisioni == null ? new DecisioniIntervalloRichiesta(null, List.of()) : decisioni);
    }

    @Override
    @Transactional
    public PrimoTempoResponse giocaPrimoTempo(Long carrieraId, Long utenteId) {
        Contesto contesto = carica(carrieraId, utenteId);
        Partita partita = contesto.partitaDellUtente();
        Squadra casa = partita.getSquadraCasa();
        Squadra trasferta = partita.getSquadraTrasferta();

        DatiPartita dati;
        if (partita.getStato() == StatoPartita.INTERVALLO) {
            dati = StatoPartitaJson.leggi(partita.getStatoSimulazione());
        } else {
            // Le energie non si salvano ora: si calcola quella con cui i giocatori scenderebbero in campo
            LocalDate data = contesto.giornata().getDataGiornata();
            dati = new DatiPartita(partita.getSeme(), casa.getStadio(),
                    squadraInCampo(casa, giocatoreRepository.findBySquadraIdOrderByCognomeAscNomeAsc(casa.getId()), data, true),
                    squadraInCampo(trasferta, giocatoreRepository.findBySquadraIdOrderByCognomeAscNomeAsc(trasferta.getId()), data, true));
            partita.setStatoSimulazione(StatoPartitaJson.scrivi(dati));
            partita.setStato(StatoPartita.INTERVALLO);
        }

        RisultatoPrimoTempo risultato = motore.primoTempo(dati);
        partita.setGolCasa(risultato.golCasa());
        partita.setGolTrasferta(risultato.golTrasferta());

        boolean utenteInCasa = casa.getId().equals(contesto.dellUtente().getId());
        Lato latoUtente = utenteInCasa ? Lato.CASA : Lato.TRASFERTA;
        Map<Long, GiocatoreInCampo> inCampo = new HashMap<>();
        dati.squadra(latoUtente).titolari().forEach(giocatore -> inCampo.put(giocatore.id(), giocatore));
        dati.squadra(latoUtente).panchina().forEach(giocatore -> inCampo.put(giocatore.id(), giocatore));
        List<SituazioneGiocatoreResponse> situazione = risultato.giocatori().stream()
                .filter(giocatore -> giocatore.lato() == latoUtente)
                .map(giocatore -> aSituazione(giocatore, inCampo.get(giocatore.giocatoreId())))
                .toList();

        return new PrimoTempoResponse(partita.getId(), casa.getNome(), trasferta.getNome(), utenteInCasa,
                risultato.golCasa(), risultato.golTrasferta(), risultato.statisticheCasa(),
                risultato.statisticheTrasferta(), risultato.eventi().stream().map(AvanzamentoServiceImpl::riga).toList(),
                situazione);
    }

    private static SituazioneGiocatoreResponse aSituazione(SituazioneGiocatore situazione, GiocatoreInCampo dati) {
        return new SituazioneGiocatoreResponse(situazione.giocatoreId(), dati.cognome(),
                dati.posizione().getSigla(), situazione.inCampo(), situazione.espulso(), situazione.ammonito(),
                situazione.infortunato(), situazione.energia(), situazione.gol(), situazione.assist(),
                situazione.tiri());
    }

    private static String riga(EventoSimulato evento) {
        String minuto = evento.minutoRecupero() > 0
                ? evento.minuto() + "+" + evento.minutoRecupero() + "'"
                : evento.minuto() + "'";
        return minuto + " " + evento.testo();
    }

    /**
     * Gioca la giornata. Con decisioni nulle la partita dell'utente si gioca di colpo; altrimenti deve essere
     * ferma all'intervallo e si prosegue con le decisioni.
     */
    private GiornataGiocataResponse giocaGiornata(Long carrieraId, Long utenteId,
            DecisioniIntervalloRichiesta decisioni) {
        Contesto contesto = carica(carrieraId, utenteId);
        Carriera carriera = contesto.carriera();
        Stagione stagione = contesto.stagione();
        Squadra dellUtente = contesto.dellUtente();
        Campionato campionato = contesto.campionato();
        LocalDate data = contesto.giornata().getDataGiornata();
        List<Partita> partite = contesto.partite();
        Partita partitaUtente = contesto.partitaDellUtente();

        boolean allIntervallo = partitaUtente.getStato() == StatoPartita.INTERVALLO;
        if (decisioni == null && allIntervallo) {
            throw new OperazioneNonValidaException(
                    "La tua partita è ferma all'intervallo: concludila indicando le tue decisioni");
        }
        if (decisioni != null && !allIntervallo) {
            throw new OperazioneNonValidaException("Nessuna partita all'intervallo: gioca prima il primo tempo");
        }

        // Le rose di tutto il campionato
        List<Giocatore> tutti = giocatoreRepository.findBySquadraCampionatoId(campionato.getId());
        Map<Long, Giocatore> giocatoriPerId = new HashMap<>();
        Map<Long, List<Giocatore>> rose = new HashMap<>();
        for (Giocatore giocatore : tutti) {
            giocatoriPerId.put(giocatore.getId(), giocatore);
            rose.computeIfAbsent(giocatore.getSquadra().getId(), id -> new ArrayList<>()).add(giocatore);
        }

        // Prima della partita: gli infortuni finiti si chiudono e tutti recuperano energia.
        // Chi è squalificato salterà questa giornata: la squalifica scende di una dopo le partite.
        List<Giocatore> squalificati = new ArrayList<>();
        for (Giocatore giocatore : tutti) {
            if (giocatore.getInfortunatoFinoAl() != null && data.isAfter(giocatore.getInfortunatoFinoAl())) {
                giocatore.setInfortunatoFinoAl(null);
                giocatore.setTipoInfortunio(null);
            }
            giocatore.setEnergia(RegoleGiornata.energiaRecuperata(giocatore.getEnergia()));
            if (giocatore.getSqualificaGiornate() > 0) {
                squalificati.add(giocatore);
            }
        }

        // Si giocano tutte le partite
        List<RisultatoPartita> risultati = new ArrayList<>();
        for (Partita partita : partite) {
            Squadra casa = partita.getSquadraCasa();
            Squadra trasferta = partita.getSquadraTrasferta();
            if (partita == partitaUtente && allIntervallo) {
                DatiPartita dati = StatoPartitaJson.leggi(partita.getStatoSimulazione());
                DecisioniIntervallo scelte = aDecisioni(decisioni);
                boolean inCasa = casa.getId().equals(dellUtente.getId());
                try {
                    risultati.add(motore.giocaPartita(dati, inCasa ? scelte : null, inCasa ? null : scelte));
                } catch (IllegalArgumentException e) {
                    throw new OperazioneNonValidaException(e.getMessage());
                }
                partita.setStatoSimulazione(null);
            } else {
                DatiPartita dati = new DatiPartita(partita.getSeme(), casa.getStadio(),
                        squadraInCampo(casa, rose.get(casa.getId()), data, false),
                        squadraInCampo(trasferta, rose.get(trasferta.getId()), data, false));
                risultati.add(motore.giocaPartita(dati));
            }
        }

        squalificati.forEach(giocatore -> giocatore.setSqualificaGiornate(giocatore.getSqualificaGiornate() - 1));

        // Si registra tutto
        Map<Long, VoceClassifica> classifica = classificaRepository.classifica(stagione.getId(), campionato.getId())
                .stream().collect(Collectors.toMap(voce -> voce.getSquadra().getId(), voce -> voce));
        List<RisultatoPartitaResponse> risposta = new ArrayList<>();
        for (int i = 0; i < partite.size(); i++) {
            Partita partita = partite.get(i);
            RisultatoPartita risultato = risultati.get(i);
            registraPartita(partita, risultato, rose, giocatoriPerId, classifica, data);
            risposta.add(new RisultatoPartitaResponse(partita.getId(), partita.getSquadraCasa().getNome(),
                    partita.getSquadraTrasferta().getNome(), risultato.golCasa(), risultato.golTrasferta(),
                    partita == partitaUtente));
        }

        applicaEconomiaEAllenamento(carriera, dellUtente, partite, rose, contesto.giornata().getNumero(), data);

        carriera.setDataCorrente(data);
        boolean conclusa = partitaRepository.prossimaGiornata(stagione.getId(), campionato.getId(), StatoPartita.CONCLUSA)
                .isEmpty();
        if (conclusa) {
            stagione.setStato(StatoStagione.CONCLUSA);
        }
        return new GiornataGiocataResponse(contesto.giornata().getNumero(), data, risposta, conclusa);
    }

    private static DecisioniIntervallo aDecisioni(DecisioniIntervalloRichiesta richiesta) {
        Istruzioni istruzioni = richiesta.istruzioni() == null ? null
                : new Istruzioni(richiesta.istruzioni().mentalita(), richiesta.istruzioni().pressing(),
                        richiesta.istruzioni().ritmo(), richiesta.istruzioni().lineaDifensiva());
        List<Sostituzione> sostituzioni = richiesta.sostituzioni() == null ? List.of()
                : richiesta.sostituzioni().stream().map(s -> new Sostituzione(s.esceId(), s.entraId())).toList();
        return DecisioniIntervallo.manuali(istruzioni, sostituzioni);
    }

    /**
     * La squadra pronta a giocare. Per quella dell'utente si usa la formazione salvata, se è ancora valida
     * (tutti i titolari disponibili); altrimenti, e per le squadre del computer, la formazione automatica.
     *
     * @param recuperaEnergia se vero l'energia mostrata è quella già recuperata per la prossima partita, senza
     *                        modificare i giocatori (serve a giocare il primo tempo prima del resto della giornata)
     */
    private SquadraInCampo squadraInCampo(Squadra squadra, List<Giocatore> rosa, LocalDate data,
            boolean recuperaEnergia) {
        Function<Giocatore, GiocatoreInCampo> inCampo = giocatore -> GiocatoreMapper.inCampo(giocatore,
                recuperaEnergia ? RegoleGiornata.energiaRecuperata(giocatore.getEnergia()) : giocatore.getEnergia());

        Optional<Formazione> salvata = squadra.isAllenataDallUtente()
                ? formazioneRepository.findBySquadraId(squadra.getId())
                : Optional.empty();
        Modulo modulo = salvata.map(Formazione::getModulo).orElse(MODULO_PREDEFINITO);
        Istruzioni istruzioni = salvata
                .map(f -> new Istruzioni(f.getMentalita(), f.getPressing(), f.getRitmo(), f.getLineaDifensiva()))
                .orElse(Istruzioni.equilibrate());

        if (salvata.isPresent()) {
            List<FormazioneGiocatore> posti = formazioneGiocatoreRepository
                    .findByFormazioneIdOrderByPostoAsc(salvata.get().getId());
            List<Giocatore> titolari = posti.stream().filter(FormazioneGiocatore::titolare)
                    .map(FormazioneGiocatore::getGiocatore).toList();
            boolean valida = titolari.size() == Modulo.TITOLARI && titolari.stream().allMatch(
                    giocatore -> giocatore.disponibileAl(data) && giocatore.getSquadra() != null
                            && giocatore.getSquadra().getId().equals(squadra.getId()));
            if (valida) {
                List<Giocatore> panchina = posti.stream().filter(posto -> !posto.titolare())
                        .map(FormazioneGiocatore::getGiocatore)
                        .filter(giocatore -> giocatore.disponibileAl(data)).toList();
                return new SquadraInCampo(squadra.getId(), squadra.getNome(), modulo, istruzioni,
                        titolari.stream().map(inCampo).toList(), panchina.stream().map(inCampo).toList());
            }
        }

        List<Giocatore> disponibili = rosa.stream().filter(giocatore -> giocatore.disponibileAl(data)).toList();
        if (disponibili.size() < Modulo.TITOLARI) {
            disponibili = rosa;
        }
        if (disponibili.size() < Modulo.TITOLARI) {
            throw new OperazioneNonValidaException("La squadra " + squadra.getNome() + " ha meno di "
                    + Modulo.TITOLARI + " giocatori");
        }
        FormazioneAutomatica.Scelta scelta = FormazioneAutomatica.scegli(
                disponibili.stream().map(inCampo).toList(), modulo);
        return new SquadraInCampo(squadra.getId(), squadra.getNome(), modulo, istruzioni, scelta.titolari(),
                scelta.panchina());
    }

    /**
     * Dopo le partite: a ogni squadra si pagano gli stipendi e chi gioca in casa incassa i biglietti
     * (il bilancio si registra solo per la squadra dell'utente); poi la squadra dell'utente si allena.
     */
    private void applicaEconomiaEAllenamento(Carriera carriera, Squadra dellUtente, List<Partita> partite,
            Map<Long, List<Giocatore>> rose, int numeroGiornata, LocalDate data) {
        int giornateTotali = 2 * (rose.size() - 1);
        for (Partita partita : partite) {
            for (Squadra squadra : List.of(partita.getSquadraCasa(), partita.getSquadraTrasferta())) {
                List<Giocatore> rosa = rose.getOrDefault(squadra.getId(), List.of());
                BigDecimal stipendi = RegoleEconomia.stipendiGiornata(
                        rosa.stream().map(Giocatore::getStipendioAnnuo).toList(), giornateTotali);
                boolean inCasa = squadra.getId().equals(partita.getSquadraCasa().getId());
                BigDecimal incasso = inCasa
                        ? RegoleEconomia.incassoBiglietti(partita.getSpettatori() == null ? 0 : partita.getSpettatori())
                        : BigDecimal.ZERO;
                squadra.setBudgetTrasferimenti(squadra.getBudgetTrasferimenti().add(incasso).subtract(stipendi));

                if (squadra.getId().equals(dellUtente.getId())) {
                    registraMovimento(carriera, squadra, data, TipoMovimento.STIPENDI, stipendi.negate(),
                            "Stipendi della giornata " + numeroGiornata);
                    if (inCasa) {
                        registraMovimento(carriera, squadra, data, TipoMovimento.BIGLIETTI, incasso,
                                "Biglietti della giornata " + numeroGiornata + " (" + partita.getSpettatori()
                                        + " spettatori)");
                    }
                }
            }
        }

        for (Giocatore giocatore : rose.getOrDefault(dellUtente.getId(), List.of())) {
            if (giocatore.infortunatoAl(data)) {
                continue;
            }
            Attributi attributi = new Attributi(giocatore.getVelocita(), giocatore.getResistenza(),
                    giocatore.getForza(), giocatore.getTecnica(), giocatore.getPassaggio(), giocatore.getTiro(),
                    giocatore.getDribbling(), giocatore.getContrasto(), giocatore.getIntelligenza(),
                    giocatore.getParate());
            long seme = carriera.getSeme() ^ (numeroGiornata * 0x9E3779B97F4A7C15L)
                    ^ (giocatore.getId() * 0xC2B2AE3D27D4EB4FL);
            RegoleAllenamento.prova(carriera.getFocusAllenamento(), giocatore.getPosizione(),
                    giocatore.etaAl(data), attributi.comeVettore(), giocatore.getValutazione(),
                    giocatore.getPotenziale(), seme).ifPresent(progresso -> migliora(giocatore, progresso, data));
        }
    }

    private void migliora(Giocatore giocatore, RegoleAllenamento.Progresso progresso, LocalDate data) {
        int valore = progresso.nuovoValore();
        switch (progresso.attributo()) {
            case VELOCITA -> giocatore.setVelocita(valore);
            case RESISTENZA -> giocatore.setResistenza(valore);
            case FORZA -> giocatore.setForza(valore);
            case TECNICA -> giocatore.setTecnica(valore);
            case PASSAGGIO -> giocatore.setPassaggio(valore);
            case TIRO -> giocatore.setTiro(valore);
            case DRIBBLING -> giocatore.setDribbling(valore);
            case CONTRASTO -> giocatore.setContrasto(valore);
            case INTELLIGENZA -> giocatore.setIntelligenza(valore);
            case PARATE -> giocatore.setParate(valore);
        }
        giocatore.setValutazione(progresso.nuovaValutazione());
        giocatore.setValoreMercato(GeneratoreCampionato.valoreDiMercato(giocatore.getValutazione(),
                giocatore.getPotenziale(), giocatore.etaAl(data)));
    }

    private void registraMovimento(Carriera carriera, Squadra squadra, LocalDate data, TipoMovimento tipo,
            BigDecimal importo, String descrizione) {
        MovimentoFinanziario movimento = new MovimentoFinanziario();
        movimento.setCarriera(carriera);
        movimento.setSquadra(squadra);
        movimento.setDataMovimento(data);
        movimento.setTipo(tipo);
        movimento.setImporto(importo);
        movimento.setDescrizione(descrizione);
        movimentoRepository.save(movimento);
    }

    private void registraPartita(Partita partita, RisultatoPartita risultato, Map<Long, List<Giocatore>> rose,
            Map<Long, Giocatore> giocatoriPerId, Map<Long, VoceClassifica> classifica, LocalDate data) {
        Squadra casa = partita.getSquadraCasa();
        Squadra trasferta = partita.getSquadraTrasferta();

        partita.setGolCasa(risultato.golCasa());
        partita.setGolTrasferta(risultato.golTrasferta());
        partita.setStato(StatoPartita.CONCLUSA);
        partita.setSpettatori(RegoleGiornata.spettatori(casa.getCapienza(), casa.getReputazione(), partita.getSeme()));

        eventoPartitaRepository.saveAll(risultato.eventi().stream()
                .map(evento -> aEvento(evento, partita, casa, trasferta, giocatoriPerId)).toList());
        statisticaPartitaRepository.saveAll(List.of(
                aStatistica(partita, casa, risultato.statisticheCasa()),
                aStatistica(partita, trasferta, risultato.statisticheTrasferta())));
        prestazioneGiocatoreRepository.saveAll(risultato.prestazioni().stream()
                .map(prestazione -> aPrestazione(prestazione, partita, casa, trasferta, giocatoriPerId)).toList());

        // Ciò che cambia nei giocatori
        for (PrestazioneSimulata prestazione : risultato.prestazioni()) {
            Giocatore giocatore = giocatoriPerId.get(prestazione.giocatoreId());
            giocatore.setEnergia(Math.max(0, Math.min(100, prestazione.energiaFinale())));
            giocatore.setForma(RegoleGiornata.nuovaForma(giocatore.getForma(), prestazione.voto()));
            if (prestazione.espulso()) {
                giocatore.setSqualificaGiornate(RegoleGiornata.GIORNATE_SQUALIFICA_ROSSO);
            }
        }
        for (InfortunioSimulato infortunio : risultato.infortuni()) {
            Giocatore giocatore = giocatoriPerId.get(infortunio.giocatoreId());
            giocatore.setInfortunatoFinoAl(data.plusDays(infortunio.giorni()));
            giocatore.setTipoInfortunio(infortunio.tipo());
        }
        rose.get(casa.getId()).forEach(g -> g.setMorale(
                RegoleGiornata.nuovoMorale(g.getMorale(), risultato.golCasa(), risultato.golTrasferta())));
        rose.get(trasferta.getId()).forEach(g -> g.setMorale(
                RegoleGiornata.nuovoMorale(g.getMorale(), risultato.golTrasferta(), risultato.golCasa())));

        // La classifica
        classifica.get(casa.getId()).registraPartita(risultato.golCasa(), risultato.golTrasferta());
        classifica.get(trasferta.getId()).registraPartita(risultato.golTrasferta(), risultato.golCasa());
    }

    private static EventoPartita aEvento(EventoSimulato evento, Partita partita, Squadra casa, Squadra trasferta,
            Map<Long, Giocatore> giocatoriPerId) {
        EventoPartita riga = new EventoPartita();
        riga.setPartita(partita);
        riga.setOrdine(evento.ordine());
        riga.setMinuto(evento.minuto());
        riga.setMinutoRecupero(evento.minutoRecupero());
        riga.setTipo(evento.tipo());
        riga.setSquadra(squadraDelLato(evento.lato(), casa, trasferta));
        riga.setGiocatore(evento.giocatoreId() == null ? null : giocatoriPerId.get(evento.giocatoreId()));
        riga.setSecondoGiocatore(
                evento.secondoGiocatoreId() == null ? null : giocatoriPerId.get(evento.secondoGiocatoreId()));
        riga.setXg(evento.xg() == null ? null : evento.xg().setScale(2, RoundingMode.HALF_UP));
        riga.setTesto(evento.testo());
        return riga;
    }

    private static StatisticaPartita aStatistica(Partita partita, Squadra squadra, StatisticheSquadra dati) {
        StatisticaPartita statistica = new StatisticaPartita();
        statistica.setPartita(partita);
        statistica.setSquadra(squadra);
        statistica.setPossesso(dati.possesso());
        statistica.setTiri(dati.tiri());
        statistica.setTiriInPorta(dati.tiriInPorta());
        statistica.setXg(BigDecimal.valueOf(dati.xg()).setScale(2, RoundingMode.HALF_UP));
        statistica.setFalli(dati.falli());
        statistica.setCalciDAngolo(dati.calciDAngolo());
        statistica.setAmmonizioni(dati.ammonizioni());
        statistica.setEspulsioni(dati.espulsioni());
        return statistica;
    }

    private static PrestazioneGiocatore aPrestazione(PrestazioneSimulata dati, Partita partita, Squadra casa,
            Squadra trasferta, Map<Long, Giocatore> giocatoriPerId) {
        PrestazioneGiocatore prestazione = new PrestazioneGiocatore();
        prestazione.setPartita(partita);
        prestazione.setGiocatore(giocatoriPerId.get(dati.giocatoreId()));
        prestazione.setSquadra(squadraDelLato(dati.lato(), casa, trasferta));
        prestazione.setTitolare(dati.titolare());
        prestazione.setMinutiGiocati(dati.minutiGiocati());
        prestazione.setGol(dati.gol());
        prestazione.setAssist(dati.assist());
        prestazione.setTiri(dati.tiri());
        prestazione.setAmmonito(dati.ammonito());
        prestazione.setEspulso(dati.espulso());
        double voto = Math.max(1.0, Math.min(10.0, dati.voto()));
        prestazione.setVoto(BigDecimal.valueOf(voto).setScale(1, RoundingMode.HALF_UP));
        return prestazione;
    }

    private static Squadra squadraDelLato(Lato lato, Squadra casa, Squadra trasferta) {
        if (lato == null) {
            return null;
        }
        return lato == Lato.CASA ? casa : trasferta;
    }
}
