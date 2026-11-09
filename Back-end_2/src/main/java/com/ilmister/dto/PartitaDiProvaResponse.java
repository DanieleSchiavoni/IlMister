package com.ilmister.dto;

import java.util.List;

import com.ilmister.motore.StatisticheSquadra;

/** Una partita giocata per prova: risultato, statistiche e cronaca (una riga per evento, con il minuto) */
public record PartitaDiProvaResponse(String casa, String trasferta, int golCasa, int golTrasferta,
        StatisticheSquadra statisticheCasa, StatisticheSquadra statisticheTrasferta, List<String> cronaca) {
}
