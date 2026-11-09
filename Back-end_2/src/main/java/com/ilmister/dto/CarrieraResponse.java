package com.ilmister.dto;

import java.time.LocalDate;

import com.ilmister.entity.enumerazioni.StatoCarriera;

/** Una carriera in sintesi: stagione, squadra allenata, data e fiducia del presidente */
public record CarrieraResponse(Long id, String nome, String nomeAllenatore, String squadra, String campionato,
        String stagione, LocalDate dataCorrente, int fiduciaPresidente, StatoCarriera stato,
        Integer prossimaGiornata, int giornateTotali) {
}
