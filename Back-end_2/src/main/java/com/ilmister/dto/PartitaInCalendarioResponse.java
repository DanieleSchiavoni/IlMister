package com.ilmister.dto;

import com.ilmister.entity.enumerazioni.StatoPartita;

/** Una partita del calendario; i gol ci sono solo se è conclusa (o all'intervallo, con il risultato parziale) */
public record PartitaInCalendarioResponse(Long id, Long squadraCasaId, String squadraCasa, Long squadraTrasfertaId,
        String squadraTrasferta, StatoPartita stato, Integer golCasa, Integer golTrasferta, boolean dellUtente) {
}
