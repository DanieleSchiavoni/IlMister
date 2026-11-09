package com.ilmister.service.impl;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.ilmister.entity.Carriera;
import com.ilmister.entity.Giornata;
import com.ilmister.entity.enumerazioni.StatoPartita;
import com.ilmister.entity.enumerazioni.StatoStagione;
import com.ilmister.repository.GiornataRepository;
import com.ilmister.repository.PartitaRepository;
import com.ilmister.repository.SquadraRepository;
import com.ilmister.repository.StagioneRepository;

import lombok.RequiredArgsConstructor;

/** Dice a che data si valuta la disponibilità dei giocatori: quella della prossima giornata da giocare */
@Component
@RequiredArgsConstructor
public class RiferimentoTemporale {

    private final StagioneRepository stagioneRepository;
    private final SquadraRepository squadraRepository;
    private final PartitaRepository partitaRepository;
    private final GiornataRepository giornataRepository;

    /** La data della prossima giornata; a stagione finita, la data attuale della carriera */
    public LocalDate dataProssimaGiornata(Carriera carriera) {
        return stagioneRepository.findByCarrieraIdAndStato(carriera.getId(), StatoStagione.IN_CORSO)
                .flatMap(stagione -> squadraRepository.findByCarrieraIdAndAllenataDallUtenteTrue(carriera.getId())
                        .flatMap(squadra -> {
                            Long campionatoId = squadra.getCampionato().getId();
                            return partitaRepository
                                    .prossimaGiornata(stagione.getId(), campionatoId, StatoPartita.CONCLUSA)
                                    .flatMap(numero -> giornataRepository.findByStagioneIdAndCampionatoIdAndNumero(
                                            stagione.getId(), campionatoId, numero));
                        }))
                .map(Giornata::getDataGiornata)
                .orElse(carriera.getDataCorrente());
    }
}
