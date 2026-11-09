package com.ilmister.service;

import java.util.List;

import com.ilmister.dto.GiornataResponse;

public interface CalendarioService {

    /** Tutte le giornate della stagione più recente, con le partite e i risultati già giocati */
    List<GiornataResponse> calendario(Long carrieraId, Long utenteId);
}
