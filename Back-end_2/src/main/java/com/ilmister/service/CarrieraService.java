package com.ilmister.service;

import java.util.List;

import com.ilmister.dto.CarrieraResponse;
import com.ilmister.dto.NuovaCarrieraRichiesta;
import com.ilmister.entity.Carriera;

public interface CarrieraService {

    /**
     * Inizia una nuova carriera: crea il campionato dal seme, le squadre con i loro giocatori e la prima
     * stagione, e assegna all'utente la squadra scelta.
     */
    Carriera creaCarriera(Long utenteId, NuovaCarrieraRichiesta richiesta);

    /** Le carriere dell'utente, le più recenti per prime */
    List<CarrieraResponse> carriere(Long utenteId);

    /** Una carriera dell'utente; chi non ne è il proprietario non la trova */
    CarrieraResponse carriera(Long carrieraId, Long utenteId);

    /** Elimina una carriera con tutti i suoi dati */
    void elimina(Long carrieraId, Long utenteId);
}
