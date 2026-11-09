package com.ilmister.service.impl;

import java.util.Arrays;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ilmister.dto.AllenamentoResponse;
import com.ilmister.entity.Carriera;
import com.ilmister.entity.enumerazioni.FocusAllenamento;
import com.ilmister.exception.RisorsaNonTrovataException;
import com.ilmister.repository.CarrieraRepository;
import com.ilmister.service.AllenamentoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AllenamentoServiceImpl implements AllenamentoService {

    private final CarrieraRepository carrieraRepository;

    @Override
    @Transactional(readOnly = true)
    public AllenamentoResponse allenamento(Long carrieraId, Long utenteId) {
        return aResponse(trova(carrieraId, utenteId));
    }

    @Override
    @Transactional
    public AllenamentoResponse impostaFocus(Long carrieraId, Long utenteId, FocusAllenamento focus) {
        Carriera carriera = trova(carrieraId, utenteId);
        carriera.setFocusAllenamento(focus);
        return aResponse(carriera);
    }

    private Carriera trova(Long carrieraId, Long utenteId) {
        return carrieraRepository.findByIdAndUtenteId(carrieraId, utenteId)
                .orElseThrow(() -> new RisorsaNonTrovataException("Carriera non trovata"));
    }

    private static AllenamentoResponse aResponse(Carriera carriera) {
        return new AllenamentoResponse(carriera.getFocusAllenamento(), Arrays.stream(FocusAllenamento.values())
                .map(f -> new AllenamentoResponse.OpzioneFocus(f, f.getNome(), f.getDescrizione())).toList());
    }
}
