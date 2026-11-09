package com.ilmister.dto;

import java.util.List;

import com.ilmister.motore.StatisticheSquadra;

/** Il primo tempo della partita dell'utente, in attesa delle sue decisioni */
public record PrimoTempoResponse(Long partitaId, String squadraCasa, String squadraTrasferta, boolean utenteInCasa,
        int golCasa, int golTrasferta, StatisticheSquadra statisticheCasa, StatisticheSquadra statisticheTrasferta,
        List<String> cronaca, List<SituazioneGiocatoreResponse> giocatoriUtente) {
}
