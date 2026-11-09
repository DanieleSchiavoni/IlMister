package com.ilmister.motore;

import java.util.List;

public record RisultatoPartita(int golCasa, int golTrasferta, List<EventoSimulato> eventi,
        StatisticheSquadra statisticheCasa, StatisticheSquadra statisticheTrasferta,
        List<PrestazioneSimulata> prestazioni, List<InfortunioSimulato> infortuni) {

    public StatisticheSquadra statistiche(Lato lato) {
        return lato == Lato.CASA ? statisticheCasa : statisticheTrasferta;
    }
}
