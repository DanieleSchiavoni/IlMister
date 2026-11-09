package com.ilmister.service;

import java.util.List;

import com.ilmister.dto.UtenteResponse;

public interface UtenteService {

    UtenteResponse profilo(Long utenteId);

    /** Solo per gli amministratori */
    List<UtenteResponse> elenco();

    /** Blocca o sblocca un utente; bloccandolo si chiudono tutte le sue sessioni */
    UtenteResponse impostaAbilitato(Long adminId, Long utenteId, boolean abilitato);
}
