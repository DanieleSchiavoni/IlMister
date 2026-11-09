package com.ilmister.service;

import java.util.List;

import com.ilmister.dto.GiocatoreResponse;

public interface RosaService {

    /** La rosa di una squadra della carriera; senza indicare la squadra, quella dell'utente */
    List<GiocatoreResponse> rosa(Long carrieraId, Long utenteId, Long squadraId);

    GiocatoreResponse giocatore(Long carrieraId, Long utenteId, Long giocatoreId);
}
