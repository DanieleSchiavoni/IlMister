package com.ilmister.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ilmister.dto.GiornataResponse;
import com.ilmister.dto.PartitaInCalendarioResponse;
import com.ilmister.entity.Giornata;
import com.ilmister.entity.Partita;
import com.ilmister.entity.Squadra;
import com.ilmister.entity.Stagione;
import com.ilmister.exception.RisorsaNonTrovataException;
import com.ilmister.repository.CarrieraRepository;
import com.ilmister.repository.PartitaRepository;
import com.ilmister.repository.SquadraRepository;
import com.ilmister.repository.StagioneRepository;
import com.ilmister.service.CalendarioService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CalendarioServiceImpl implements CalendarioService {

    private final CarrieraRepository carrieraRepository;
    private final StagioneRepository stagioneRepository;
    private final SquadraRepository squadraRepository;
    private final PartitaRepository partitaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<GiornataResponse> calendario(Long carrieraId, Long utenteId) {
        carrieraRepository.findByIdAndUtenteId(carrieraId, utenteId)
                .orElseThrow(() -> new RisorsaNonTrovataException("Carriera non trovata"));
        Stagione stagione = stagioneRepository.findByCarrieraIdOrderByAnnoInizioDesc(carrieraId).stream().findFirst()
                .orElseThrow(() -> new RisorsaNonTrovataException("Stagione non trovata"));
        Squadra dellUtente = squadraRepository.findByCarrieraIdAndAllenataDallUtenteTrue(carrieraId)
                .orElseThrow(() -> new RisorsaNonTrovataException("Squadra dell'utente non trovata"));

        Map<Integer, List<Partita>> perGiornata = new LinkedHashMap<>();
        for (Partita partita : partitaRepository.calendario(stagione.getId(), dellUtente.getCampionato().getId())) {
            perGiornata.computeIfAbsent(partita.getGiornata().getNumero(), numero -> new ArrayList<>()).add(partita);
        }

        List<GiornataResponse> risposta = new ArrayList<>();
        perGiornata.forEach((numero, partite) -> {
            Giornata giornata = partite.get(0).getGiornata();
            risposta.add(new GiornataResponse(numero, giornata.getDataGiornata(),
                    partite.stream().map(partita -> aResponse(partita, dellUtente)).toList()));
        });
        return risposta;
    }

    private static PartitaInCalendarioResponse aResponse(Partita partita, Squadra dellUtente) {
        Squadra casa = partita.getSquadraCasa();
        Squadra trasferta = partita.getSquadraTrasferta();
        return new PartitaInCalendarioResponse(partita.getId(), casa.getId(), casa.getNome(), trasferta.getId(),
                trasferta.getNome(), partita.getStato(), partita.getGolCasa(), partita.getGolTrasferta(),
                casa.getId().equals(dellUtente.getId()) || trasferta.getId().equals(dellUtente.getId()));
    }
}
