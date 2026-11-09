package com.ilmister.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ilmister.dto.EventoResponse;
import com.ilmister.dto.PartitaDettaglioResponse;
import com.ilmister.dto.PrestazioneResponse;
import com.ilmister.dto.StatisticaResponse;
import com.ilmister.entity.Partita;
import com.ilmister.entity.Squadra;
import com.ilmister.entity.enumerazioni.StatoPartita;
import com.ilmister.exception.OperazioneNonValidaException;
import com.ilmister.exception.RisorsaNonTrovataException;
import com.ilmister.repository.CarrieraRepository;
import com.ilmister.repository.EventoPartitaRepository;
import com.ilmister.repository.PartitaRepository;
import com.ilmister.repository.PrestazioneGiocatoreRepository;
import com.ilmister.repository.StatisticaPartitaRepository;
import com.ilmister.service.PartitaService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PartitaServiceImpl implements PartitaService {

    private final CarrieraRepository carrieraRepository;
    private final PartitaRepository partitaRepository;
    private final EventoPartitaRepository eventoPartitaRepository;
    private final StatisticaPartitaRepository statisticaPartitaRepository;
    private final PrestazioneGiocatoreRepository prestazioneGiocatoreRepository;

    @Override
    @Transactional(readOnly = true)
    public PartitaDettaglioResponse resoconto(Long carrieraId, Long utenteId, Long partitaId) {
        carrieraRepository.findByIdAndUtenteId(carrieraId, utenteId)
                .orElseThrow(() -> new RisorsaNonTrovataException("Carriera non trovata"));
        Partita partita = partitaRepository.findByIdAndGiornataStagioneCarrieraId(partitaId, carrieraId)
                .orElseThrow(() -> new RisorsaNonTrovataException("Partita non trovata"));
        if (partita.getStato() != StatoPartita.CONCLUSA) {
            throw new OperazioneNonValidaException("La partita non è ancora conclusa");
        }
        Squadra casa = partita.getSquadraCasa();
        Squadra trasferta = partita.getSquadraTrasferta();

        List<StatisticaResponse> statistiche = statisticaPartitaRepository.findByPartitaId(partita.getId()).stream()
                .map(s -> new StatisticaResponse(s.getSquadra().getId(), s.getSquadra().getNome(), s.getPossesso(),
                        s.getTiri(), s.getTiriInPorta(), s.getXg(), s.getFalli(), s.getCalciDAngolo(),
                        s.getAmmonizioni(), s.getEspulsioni()))
                .toList();
        List<EventoResponse> cronaca = eventoPartitaRepository.findByPartitaIdOrderByOrdineAsc(partita.getId()).stream()
                .map(e -> new EventoResponse(e.getMinuto(), e.getMinutoRecupero(), e.getTipo(),
                        e.getSquadra() == null ? null : e.getSquadra().getId(), e.getTesto()))
                .toList();
        List<PrestazioneResponse> prestazioni = prestazioneGiocatoreRepository.findByPartitaId(partita.getId()).stream()
                .map(p -> new PrestazioneResponse(p.getGiocatore().getId(), p.getGiocatore().nomeCompleto(),
                        p.getGiocatore().getPosizione().getSigla(), p.getSquadra().getId(), p.isTitolare(),
                        p.getMinutiGiocati(), p.getGol(), p.getAssist(), p.getTiri(), p.isAmmonito(), p.isEspulso(),
                        p.getVoto()))
                .toList();

        return new PartitaDettaglioResponse(partita.getId(), partita.getGiornata().getNumero(),
                partita.getGiornata().getDataGiornata(), casa.getStadio(), partita.getSpettatori(), partita.getStato(),
                casa.getId(), casa.getNome(), trasferta.getId(), trasferta.getNome(), partita.getGolCasa(),
                partita.getGolTrasferta(), statistiche, cronaca, prestazioni);
    }
}
