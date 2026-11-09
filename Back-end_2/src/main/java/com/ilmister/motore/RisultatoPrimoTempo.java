package com.ilmister.motore;

import java.util.List;

public record RisultatoPrimoTempo(int golCasa, int golTrasferta, List<EventoSimulato> eventi,
        StatisticheSquadra statisticheCasa, StatisticheSquadra statisticheTrasferta,
        List<SituazioneGiocatore> giocatori) {
}
