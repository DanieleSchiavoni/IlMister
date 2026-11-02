package com.ilmister.service;

import com.ilmister.dto.NuovaCarrieraRichiesta;
import com.ilmister.entity.Carriera;

public interface CarrieraService {

    /**
     * Inizia una nuova carriera: crea il campionato dal seme, le squadre con i loro giocatori e la prima
     * stagione, e assegna all'utente la squadra scelta.
     */
    Carriera creaCarriera(Long utenteId, NuovaCarrieraRichiesta richiesta);
}
