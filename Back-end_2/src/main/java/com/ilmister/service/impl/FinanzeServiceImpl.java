package com.ilmister.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ilmister.dto.FinanzeResponse;
import com.ilmister.dto.MovimentoResponse;
import com.ilmister.entity.Giocatore;
import com.ilmister.entity.Squadra;
import com.ilmister.exception.RisorsaNonTrovataException;
import com.ilmister.repository.CarrieraRepository;
import com.ilmister.repository.GiocatoreRepository;
import com.ilmister.repository.MovimentoFinanziarioRepository;
import com.ilmister.repository.SquadraRepository;
import com.ilmister.service.FinanzeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FinanzeServiceImpl implements FinanzeService {

    private final CarrieraRepository carrieraRepository;
    private final SquadraRepository squadraRepository;
    private final GiocatoreRepository giocatoreRepository;
    private final MovimentoFinanziarioRepository movimentoRepository;

    @Override
    @Transactional(readOnly = true)
    public FinanzeResponse finanze(Long carrieraId, Long utenteId) {
        carrieraRepository.findByIdAndUtenteId(carrieraId, utenteId)
                .orElseThrow(() -> new RisorsaNonTrovataException("Carriera non trovata"));
        Squadra squadra = squadraRepository.findByCarrieraIdAndAllenataDallUtenteTrue(carrieraId)
                .orElseThrow(() -> new RisorsaNonTrovataException("Squadra dell'utente non trovata"));
        List<Giocatore> rosa = giocatoreRepository.findBySquadraIdOrderByCognomeAscNomeAsc(squadra.getId());

        BigDecimal monteStipendi = rosa.stream().map(Giocatore::getStipendioAnnuo).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal valoreRosa = rosa.stream().map(Giocatore::getValoreMercato).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal entrate = movimentoRepository.totaleEntrate(squadra.getId()).orElse(BigDecimal.ZERO);
        // Le uscite sono salvate con il segno meno: qui si mostrano come cifra positiva
        BigDecimal uscite = movimentoRepository.totaleUscite(squadra.getId()).orElse(BigDecimal.ZERO).negate();
        List<MovimentoResponse> movimenti = movimentoRepository
                .findTop30BySquadraIdOrderByDataMovimentoDescIdDesc(squadra.getId()).stream()
                .map(m -> new MovimentoResponse(m.getId(), m.getDataMovimento(), m.getTipo(), m.getImporto(),
                        m.getDescrizione()))
                .toList();
        return new FinanzeResponse(squadra.getBudgetTrasferimenti(), monteStipendi, valoreRosa, rosa.size(), entrate,
                uscite, movimenti);
    }
}
