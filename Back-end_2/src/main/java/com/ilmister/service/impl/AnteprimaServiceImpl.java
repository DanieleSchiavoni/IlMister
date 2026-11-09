package com.ilmister.service.impl;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;

import com.ilmister.dto.AnteprimaCampionatoResponse;
import com.ilmister.dto.AnteprimaGiocatoreResponse;
import com.ilmister.dto.AnteprimaRosaResponse;
import com.ilmister.dto.AnteprimaSquadraResponse;
import com.ilmister.dto.PartitaDiProvaResponse;
import com.ilmister.entity.enumerazioni.Modulo;
import com.ilmister.exception.OperazioneNonValidaException;
import com.ilmister.exception.RisorsaNonTrovataException;
import com.ilmister.generazione.CampionatoGenerato;
import com.ilmister.generazione.ConfigurazioneGenerazione;
import com.ilmister.generazione.GeneratoreCampionato;
import com.ilmister.generazione.GiocatoreGenerato;
import com.ilmister.generazione.SquadraGenerata;
import com.ilmister.motore.DatiPartita;
import com.ilmister.motore.EventoSimulato;
import com.ilmister.motore.FormazioneAutomatica;
import com.ilmister.motore.GiocatoreInCampo;
import com.ilmister.motore.Istruzioni;
import com.ilmister.motore.MotorePartita;
import com.ilmister.motore.RisultatoPartita;
import com.ilmister.motore.SquadraInCampo;
import com.ilmister.service.AnteprimaService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnteprimaServiceImpl implements AnteprimaService {

    private static final int GIOCATORI_IN_ANTEPRIMA = 3;

    private final GeneratoreCampionato generatore;
    private final MotorePartita motore;
    private final Clock clock;

    @Override
    public AnteprimaCampionatoResponse anteprima(long seme) {
        LocalDate inizio = CarrieraServiceImpl.inizioStagione(clock);
        CampionatoGenerato campionato = generatore.genera(seme, ConfigurazioneGenerazione.predefinita(), inizio);
        List<SquadraGenerata> squadre = campionato.squadre();

        List<AnteprimaSquadraResponse> risposta = IntStream.range(0, squadre.size())
                .mapToObj(i -> aSquadra(i, squadre.get(i), inizio))
                .toList();
        return new AnteprimaCampionatoResponse(campionato.nome(), seme, risposta);
    }

    @Override
    public AnteprimaRosaResponse rosa(long seme, int indiceSquadra) {
        LocalDate inizio = CarrieraServiceImpl.inizioStagione(clock);
        CampionatoGenerato campionato = generatore.genera(seme, ConfigurazioneGenerazione.predefinita(), inizio);
        if (indiceSquadra < 0 || indiceSquadra >= campionato.squadre().size()) {
            throw new RisorsaNonTrovataException("Squadra non trovata");
        }
        SquadraGenerata squadra = campionato.squadre().get(indiceSquadra);
        List<AnteprimaGiocatoreResponse> giocatori = squadra.giocatori().stream()
                .map(giocatore -> aGiocatore(giocatore, inizio))
                .toList();
        return new AnteprimaRosaResponse(squadra.nome(), giocatori);
    }

    private AnteprimaSquadraResponse aSquadra(int indice, SquadraGenerata squadra, LocalDate inizio) {
        List<AnteprimaGiocatoreResponse> migliori = squadra.giocatori().stream()
                .sorted(Comparator.comparingInt(GiocatoreGenerato::valutazione).reversed())
                .limit(GIOCATORI_IN_ANTEPRIMA)
                .map(giocatore -> aGiocatore(giocatore, inizio))
                .toList();
        double forza = Math.round(squadra.forzaMedia() * 10) / 10.0;
        return new AnteprimaSquadraResponse(indice, squadra.nome(), squadra.citta(), squadra.stadio(),
                squadra.capienza(), squadra.reputazione(), forza, migliori);
    }

    private AnteprimaGiocatoreResponse aGiocatore(GiocatoreGenerato giocatore, LocalDate inizio) {
        int eta = java.time.Period.between(giocatore.dataNascita(), inizio).getYears();
        return new AnteprimaGiocatoreResponse(giocatore.numeroMaglia(), giocatore.nome() + " " + giocatore.cognome(),
                giocatore.posizione().getSigla(), eta, giocatore.valutazione(), giocatore.potenziale());
    }

    @Override
    public PartitaDiProvaResponse partitaDiProva(long seme, int indiceCasa, int indiceTrasferta, long semePartita) {
        LocalDate inizio = CarrieraServiceImpl.inizioStagione(clock);
        List<SquadraGenerata> squadre = generatore.genera(seme, ConfigurazioneGenerazione.predefinita(), inizio).squadre();
        if (indiceCasa < 0 || indiceCasa >= squadre.size() || indiceTrasferta < 0 || indiceTrasferta >= squadre.size()) {
            throw new RisorsaNonTrovataException("Squadra non trovata");
        }
        if (indiceCasa == indiceTrasferta) {
            throw new OperazioneNonValidaException("Una squadra non può giocare contro se stessa");
        }
        SquadraGenerata casa = squadre.get(indiceCasa);
        SquadraGenerata trasferta = squadre.get(indiceTrasferta);

        RisultatoPartita risultato = motore.giocaPartita(new DatiPartita(semePartita, casa.stadio(),
                squadraInCampo(casa, 1_000L), squadraInCampo(trasferta, 2_000L)));

        List<String> cronaca = risultato.eventi().stream().map(AnteprimaServiceImpl::riga).toList();
        return new PartitaDiProvaResponse(casa.nome(), trasferta.nome(), risultato.golCasa(), risultato.golTrasferta(),
                risultato.statisticheCasa(), risultato.statisticheTrasferta(), cronaca);
    }

    /** La squadra in 4-3-3, con la formazione scelta in automatico tra tutta la rosa */
    private SquadraInCampo squadraInCampo(SquadraGenerata squadra, long idBase) {
        List<GiocatoreInCampo> rosa = new java.util.ArrayList<>();
        long id = idBase;
        for (GiocatoreGenerato giocatore : squadra.giocatori()) {
            id++;
            rosa.add(new GiocatoreInCampo(id, giocatore.cognome(), giocatore.posizione(), giocatore.attributi(), 50, 70,
                    100, giocatore.numeroMaglia()));
        }
        FormazioneAutomatica.Scelta scelta = FormazioneAutomatica.scegli(rosa, Modulo.M_4_3_3);
        return new SquadraInCampo(idBase, squadra.nome(), Modulo.M_4_3_3, Istruzioni.equilibrate(), scelta.titolari(),
                scelta.panchina());
    }

    private static String riga(EventoSimulato evento) {
        String minuto = evento.minutoRecupero() > 0
                ? evento.minuto() + "+" + evento.minutoRecupero() + "'"
                : evento.minuto() + "'";
        return minuto + " " + evento.testo();
    }
}
