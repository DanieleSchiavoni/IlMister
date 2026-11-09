package com.ilmister.service;

import java.util.List;

import com.ilmister.dto.RigaClassificaResponse;

public interface ClassificaService {

    /** La classifica attuale del campionato della squadra dell'utente, in ordine */
    List<RigaClassificaResponse> classifica(Long carrieraId, Long utenteId);
}
