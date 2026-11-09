package com.ilmister.service.impl;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ilmister.dto.GiocatoreResponse;
import com.ilmister.entity.Carriera;
import com.ilmister.entity.Giocatore;
import com.ilmister.entity.Squadra;
import com.ilmister.exception.RisorsaNonTrovataException;
import com.ilmister.repository.CarrieraRepository;
import com.ilmister.repository.GiocatoreRepository;
import com.ilmister.repository.SquadraRepository;
import com.ilmister.service.RosaService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RosaServiceImpl implements RosaService {

    private final CarrieraRepository carrieraRepository;
    private final SquadraRepository squadraRepository;
    private final GiocatoreRepository giocatoreRepository;
    private final RiferimentoTemporale riferimentoTemporale;

    @Override
    @Transactional(readOnly = true)
    public List<GiocatoreResponse> rosa(Long carrieraId, Long utenteId, Long squadraId) {
        Carriera carriera = trovaCarriera(carrieraId, utenteId);
        Squadra squadra = squadraId == null
                ? squadraRepository.findByCarrieraIdAndAllenataDallUtenteTrue(carrieraId)
                        .orElseThrow(() -> new RisorsaNonTrovataException("Squadra non trovata"))
                : squadraRepository.findByIdAndCarrieraId(squadraId, carrieraId)
                        .orElseThrow(() -> new RisorsaNonTrovataException("Squadra non trovata"));
        LocalDate data = riferimentoTemporale.dataProssimaGiornata(carriera);

        return giocatoreRepository.findBySquadraIdOrderByCognomeAscNomeAsc(squadra.getId()).stream()
                .sorted(Comparator.comparing(Giocatore::getRuolo)
                        .thenComparing(Comparator.comparingInt(Giocatore::getValutazione).reversed()))
                .map(giocatore -> GiocatoreMapper.aResponse(giocatore, data))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GiocatoreResponse giocatore(Long carrieraId, Long utenteId, Long giocatoreId) {
        Carriera carriera = trovaCarriera(carrieraId, utenteId);
        Giocatore giocatore = giocatoreRepository.findByIdAndCarrieraId(giocatoreId, carrieraId)
                .orElseThrow(() -> new RisorsaNonTrovataException("Giocatore non trovato"));
        return GiocatoreMapper.aResponse(giocatore, riferimentoTemporale.dataProssimaGiornata(carriera));
    }

    private Carriera trovaCarriera(Long carrieraId, Long utenteId) {
        return carrieraRepository.findByIdAndUtenteId(carrieraId, utenteId)
                .orElseThrow(() -> new RisorsaNonTrovataException("Carriera non trovata"));
    }
}
