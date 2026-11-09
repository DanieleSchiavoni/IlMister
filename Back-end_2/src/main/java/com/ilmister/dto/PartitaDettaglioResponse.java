package com.ilmister.dto;

import java.time.LocalDate;
import java.util.List;

import com.ilmister.entity.enumerazioni.StatoPartita;

/** Il resoconto di una partita conclusa: risultato, statistiche, cronaca minuto per minuto e prestazioni */
public record PartitaDettaglioResponse(Long id, int giornata, LocalDate data, String stadio, Integer spettatori,
        StatoPartita stato, Long squadraCasaId, String squadraCasa, Long squadraTrasfertaId, String squadraTrasferta,
        Integer golCasa, Integer golTrasferta, List<StatisticaResponse> statistiche, List<EventoResponse> cronaca,
        List<PrestazioneResponse> prestazioni) {
}
