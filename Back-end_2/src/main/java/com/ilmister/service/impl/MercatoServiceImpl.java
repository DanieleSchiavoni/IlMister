package com.ilmister.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SplittableRandom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ilmister.dto.EsitoTrattativaResponse;
import com.ilmister.dto.GiocatoreMercatoResponse;
import com.ilmister.entity.Carriera;
import com.ilmister.entity.Giocatore;
import com.ilmister.entity.MovimentoFinanziario;
import com.ilmister.entity.Squadra;
import com.ilmister.entity.enumerazioni.Posizione;
import com.ilmister.entity.enumerazioni.Ruolo;
import com.ilmister.entity.enumerazioni.TipoMovimento;
import com.ilmister.exception.OperazioneNonValidaException;
import com.ilmister.exception.RisorsaNonTrovataException;
import com.ilmister.repository.CarrieraRepository;
import com.ilmister.repository.FormazioneGiocatoreRepository;
import com.ilmister.repository.FormazioneRepository;
import com.ilmister.repository.GiocatoreRepository;
import com.ilmister.repository.MovimentoFinanziarioRepository;
import com.ilmister.repository.SquadraRepository;
import com.ilmister.service.MercatoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MercatoServiceImpl implements MercatoService {

    static final int MASSIMO_RISULTATI = 60;

    private final CarrieraRepository carrieraRepository;
    private final SquadraRepository squadraRepository;
    private final GiocatoreRepository giocatoreRepository;
    private final FormazioneRepository formazioneRepository;
    private final FormazioneGiocatoreRepository formazioneGiocatoreRepository;
    private final MovimentoFinanziarioRepository movimentoRepository;

    private record Contesto(Carriera carriera, Squadra mia) {
        LocalDate oggi() {
            return carriera.getDataCorrente();
        }
    }

    private Contesto contesto(Long carrieraId, Long utenteId) {
        Carriera carriera = carrieraRepository.findByIdAndUtenteId(carrieraId, utenteId)
                .orElseThrow(() -> new RisorsaNonTrovataException("Carriera non trovata"));
        Squadra mia = squadraRepository.findByCarrieraIdAndAllenataDallUtenteTrue(carrieraId)
                .orElseThrow(() -> new RisorsaNonTrovataException("Squadra dell'utente non trovata"));
        return new Contesto(carriera, mia);
    }

    private Giocatore giocatore(Long carrieraId, Long giocatoreId) {
        return giocatoreRepository.findByIdAndCarrieraId(giocatoreId, carrieraId)
                .orElseThrow(() -> new RisorsaNonTrovataException("Giocatore non trovato"));
    }

    // ---------- ricerca ----------

    @Override
    @Transactional(readOnly = true)
    public List<GiocatoreMercatoResponse> cerca(Long carrieraId, Long utenteId, Ruolo ruolo, String nome,
            BigDecimal valoreMassimo) {
        Contesto contesto = contesto(carrieraId, utenteId);
        String ricerca = nome == null ? "" : nome.trim().toLowerCase();
        return giocatoreRepository.dellAltreSquadre(carrieraId, contesto.mia().getId()).stream()
                .filter(g -> ruolo == null || g.getRuolo() == ruolo)
                .filter(g -> ricerca.isEmpty() || g.nomeCompleto().toLowerCase().contains(ricerca))
                .filter(g -> valoreMassimo == null || g.getValoreMercato().compareTo(valoreMassimo) <= 0)
                .limit(MASSIMO_RISULTATI)
                .map(g -> aResponse(g, contesto.oggi()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GiocatoreMercatoResponse> svincolati(Long carrieraId, Long utenteId) {
        Contesto contesto = contesto(carrieraId, utenteId);
        return giocatoreRepository.findByCarrieraIdAndSquadraIsNull(carrieraId).stream()
                .sorted(Comparator.comparingInt(Giocatore::getValutazione).reversed().thenComparing(Giocatore::getId))
                .limit(MASSIMO_RISULTATI)
                .map(g -> aResponse(g, contesto.oggi()))
                .toList();
    }

    // ---------- acquisto ----------

    @Override
    @Transactional
    public EsitoTrattativaResponse offri(Long carrieraId, Long utenteId, Long giocatoreId, BigDecimal importo) {
        Contesto contesto = contesto(carrieraId, utenteId);
        Squadra mia = contesto.mia();
        Giocatore giocatore = giocatore(carrieraId, giocatoreId);
        if (giocatore.getSquadra() == null) {
            throw new OperazioneNonValidaException("Il giocatore è svincolato: si ingaggia senza offerta");
        }
        if (giocatore.getSquadra().getId().equals(mia.getId())) {
            throw new OperazioneNonValidaException("Il giocatore è già della tua squadra");
        }
        BigDecimal offerta = importo.setScale(2, RoundingMode.HALF_UP);
        if (offerta.compareTo(mia.getBudgetTrasferimenti()) > 0) {
            throw new OperazioneNonValidaException("Il budget non basta per questa offerta");
        }
        controllaPostoInRosa(mia);

        Squadra venditrice = giocatore.getSquadra();
        if (giocatoreRepository.countBySquadraId(venditrice.getId()) <= RegoleMercato.ROSA_MINIMA) {
            return rifiuto(mia, venditrice.getNome() + " non vuole restare con una rosa così corta");
        }
        boolean titolare = giocatoreRepository.findBySquadraIdOrderByCognomeAscNomeAsc(venditrice.getId()).stream()
                .sorted(Comparator.comparingInt(Giocatore::getValutazione).reversed().thenComparing(Giocatore::getId))
                .limit(11)
                .anyMatch(g -> g.getId().equals(giocatore.getId()));

        BigDecimal soglia = RegoleMercato.soglia(giocatore.getValoreMercato(), titolare,
                contesto.carriera().getSeme(), giocatore.getId(), contesto.oggi());
        RegoleMercato.Giudizio giudizio = RegoleMercato.giudica(offerta, soglia);
        if (giudizio != RegoleMercato.Giudizio.ACCETTATA) {
            return rifiuto(mia, giudizio == RegoleMercato.Giudizio.VICINA
                    ? venditrice.getNome() + " rifiuta, ma la trattativa è vicina: prova a rilanciare"
                    : venditrice.getNome() + " rifiuta: l'offerta è troppo lontana dalle sue richieste");
        }

        venditrice.setBudgetTrasferimenti(venditrice.getBudgetTrasferimenti().add(offerta));
        mia.setBudgetTrasferimenti(mia.getBudgetTrasferimenti().subtract(offerta));
        trasferisci(giocatore, mia);
        registra(contesto, TipoMovimento.ACQUISTO, offerta.negate(),
                "Acquisto di " + giocatore.nomeCompleto() + " da " + venditrice.getNome());
        return new EsitoTrattativaResponse(true, giocatore.nomeCompleto() + " è un nuovo giocatore della tua squadra",
                mia.getBudgetTrasferimenti());
    }

    @Override
    @Transactional
    public EsitoTrattativaResponse ingaggia(Long carrieraId, Long utenteId, Long giocatoreId) {
        Contesto contesto = contesto(carrieraId, utenteId);
        Giocatore giocatore = giocatore(carrieraId, giocatoreId);
        if (giocatore.getSquadra() != null) {
            throw new OperazioneNonValidaException("Il giocatore ha già una squadra");
        }
        controllaPostoInRosa(contesto.mia());
        trasferisci(giocatore, contesto.mia());
        return new EsitoTrattativaResponse(true, giocatore.nomeCompleto() + " ha firmato con la tua squadra",
                contesto.mia().getBudgetTrasferimenti());
    }

    // ---------- cessione ----------

    @Override
    @Transactional
    public EsitoTrattativaResponse vendi(Long carrieraId, Long utenteId, Long giocatoreId) {
        Contesto contesto = contesto(carrieraId, utenteId);
        Squadra mia = contesto.mia();
        Giocatore giocatore = daCedere(mia, carrieraId, giocatoreId);
        BigDecimal prezzo = RegoleMercato.prezzoVendita(giocatore.getValoreMercato());

        List<Squadra> possibili = squadraRepository.findByCampionatoIdOrderByNomeAsc(mia.getCampionato().getId())
                .stream()
                .filter(s -> !s.getId().equals(mia.getId()))
                .filter(s -> s.getBudgetTrasferimenti().compareTo(prezzo) >= 0)
                .filter(s -> giocatoreRepository.countBySquadraId(s.getId()) < RegoleMercato.ROSA_MASSIMA)
                .toList();
        if (possibili.isEmpty()) {
            return rifiuto(mia, "Nessuna squadra può permettersi " + giocatore.nomeCompleto() + " in questo momento");
        }
        long seme = contesto.carriera().getSeme() ^ (giocatore.getId() * 0x9E3779B97F4A7C15L)
                ^ contesto.oggi().toEpochDay();
        Squadra acquirente = possibili.get(new SplittableRandom(seme).nextInt(possibili.size()));

        acquirente.setBudgetTrasferimenti(acquirente.getBudgetTrasferimenti().subtract(prezzo));
        mia.setBudgetTrasferimenti(mia.getBudgetTrasferimenti().add(prezzo));
        trasferisci(giocatore, acquirente);
        registra(contesto, TipoMovimento.VENDITA, prezzo,
                "Cessione di " + giocatore.nomeCompleto() + " a " + acquirente.getNome());
        // Va per ultimo: la cancellazione della formazione svuota la cache di JPA
        String avviso = togliDallaFormazione(mia, giocatore);
        return new EsitoTrattativaResponse(true, giocatore.nomeCompleto() + " passa a " + acquirente.getNome()
                + " per " + prezzo.toPlainString() + avviso, mia.getBudgetTrasferimenti());
    }

    @Override
    @Transactional
    public EsitoTrattativaResponse svincola(Long carrieraId, Long utenteId, Long giocatoreId) {
        Contesto contesto = contesto(carrieraId, utenteId);
        Giocatore giocatore = daCedere(contesto.mia(), carrieraId, giocatoreId);
        giocatore.setSquadra(null);
        giocatore.setNumeroMaglia(null);
        // Va per ultimo: la cancellazione della formazione svuota la cache di JPA
        String avviso = togliDallaFormazione(contesto.mia(), giocatore);
        return new EsitoTrattativaResponse(true, giocatore.nomeCompleto() + " è stato svincolato" + avviso,
                contesto.mia().getBudgetTrasferimenti());
    }

    // ---------- regole comuni ----------

    /** Un giocatore dell'utente che si può cedere: la rosa non deve scendere sotto i minimi */
    private Giocatore daCedere(Squadra mia, Long carrieraId, Long giocatoreId) {
        Giocatore giocatore = giocatore(carrieraId, giocatoreId);
        if (giocatore.getSquadra() == null || !giocatore.getSquadra().getId().equals(mia.getId())) {
            throw new OperazioneNonValidaException("Il giocatore non è della tua squadra");
        }
        List<Giocatore> rosa = giocatoreRepository.findBySquadraIdOrderByCognomeAscNomeAsc(mia.getId());
        if (rosa.size() <= RegoleMercato.ROSA_MINIMA) {
            throw new OperazioneNonValidaException(
                    "La rosa non può scendere sotto i " + RegoleMercato.ROSA_MINIMA + " giocatori");
        }
        if (giocatore.getPosizione() == Posizione.PORTIERE && rosa.stream()
                .filter(g -> g.getPosizione() == Posizione.PORTIERE).count() <= RegoleMercato.PORTIERI_MINIMI) {
            throw new OperazioneNonValidaException(
                    "Servono almeno " + RegoleMercato.PORTIERI_MINIMI + " portieri in rosa");
        }
        return giocatore;
    }

    private void controllaPostoInRosa(Squadra mia) {
        if (giocatoreRepository.countBySquadraId(mia.getId()) >= RegoleMercato.ROSA_MASSIMA) {
            throw new OperazioneNonValidaException(
                    "La rosa ha già il massimo di " + RegoleMercato.ROSA_MASSIMA + " giocatori");
        }
    }

    /** Se il giocatore era nella formazione salvata, la formazione si azzera (resta la proposta automatica) */
    private String togliDallaFormazione(Squadra mia, Giocatore giocatore) {
        if (!formazioneGiocatoreRepository.existsByFormazioneSquadraIdAndGiocatoreId(mia.getId(), giocatore.getId())) {
            return "";
        }
        formazioneRepository.findBySquadraId(mia.getId()).ifPresent(formazione -> {
            formazioneGiocatoreRepository.eliminaPerFormazione(formazione.getId());
            formazioneRepository.delete(formazione);
        });
        return ". La formazione salvata è stata azzerata: rifalla prima della prossima partita";
    }

    /** Il giocatore cambia squadra e prende un numero di maglia libero (il suo, se non è già usato) */
    private void trasferisci(Giocatore giocatore, Squadra nuova) {
        Set<Integer> usati = new HashSet<>();
        giocatoreRepository.findBySquadraIdOrderByCognomeAscNomeAsc(nuova.getId())
                .forEach(g -> usati.add(g.getNumeroMaglia()));
        Integer numero = giocatore.getNumeroMaglia();
        if (numero == null || usati.contains(numero)) {
            numero = 1;
            while (usati.contains(numero)) {
                numero++;
            }
        }
        giocatore.setSquadra(nuova);
        giocatore.setNumeroMaglia(numero);
    }

    private void registra(Contesto contesto, TipoMovimento tipo, BigDecimal importo, String descrizione) {
        MovimentoFinanziario movimento = new MovimentoFinanziario();
        movimento.setCarriera(contesto.carriera());
        movimento.setSquadra(contesto.mia());
        movimento.setDataMovimento(contesto.oggi());
        movimento.setTipo(tipo);
        movimento.setImporto(importo);
        movimento.setDescrizione(descrizione.length() > 120 ? descrizione.substring(0, 120) : descrizione);
        movimentoRepository.save(movimento);
    }

    private static EsitoTrattativaResponse rifiuto(Squadra mia, String messaggio) {
        return new EsitoTrattativaResponse(false, messaggio, mia.getBudgetTrasferimenti());
    }

    private static GiocatoreMercatoResponse aResponse(Giocatore g, LocalDate oggi) {
        Squadra squadra = g.getSquadra();
        return new GiocatoreMercatoResponse(g.getId(), g.getNome(), g.getCognome(), g.getPosizione().getSigla(),
                g.getRuolo().name(), g.etaAl(oggi), g.getValutazione(), g.getPotenziale(), g.getValoreMercato(),
                g.getStipendioAnnuo(), g.getScadenzaContratto(), squadra == null ? null : squadra.getId(),
                squadra == null ? null : squadra.getNome());
    }
}
