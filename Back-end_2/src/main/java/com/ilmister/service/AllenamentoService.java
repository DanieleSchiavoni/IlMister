package com.ilmister.service;

import com.ilmister.dto.AllenamentoResponse;
import com.ilmister.entity.enumerazioni.FocusAllenamento;

public interface AllenamentoService {

    AllenamentoResponse allenamento(Long carrieraId, Long utenteId);

    AllenamentoResponse impostaFocus(Long carrieraId, Long utenteId, FocusAllenamento focus);
}
