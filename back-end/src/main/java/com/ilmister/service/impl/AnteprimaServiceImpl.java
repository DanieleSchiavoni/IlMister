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
import com.ilmister.exception.RisorsaNonTrovataException;
import com.ilmister.generazione.CampionatoGenerato;
import com.ilmister.generazione.ConfigurazioneGenerazione;
import com.ilmister.generazione.GeneratoreCampionato;
import com.ilmister.generazione.GiocatoreGenerato;
import com.ilmister.generazione.SquadraGenerata;
import com.ilmister.service.AnteprimaService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnteprimaServiceImpl implements AnteprimaService {

    private static final int GIOCATORI_IN_ANTEPRIMA = 3;

    private final GeneratoreCampionato generatore;
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
}
