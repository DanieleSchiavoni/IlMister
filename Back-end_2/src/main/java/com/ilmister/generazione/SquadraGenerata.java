package com.ilmister.generazione;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

public record SquadraGenerata(String nome, String nomeBreve, String citta, String stadio, int capienza,
        int reputazione, String colorePrimario, String coloreSecondario, BigDecimal budgetTrasferimenti,
        List<GiocatoreGenerato> giocatori) {

    /** La forza della squadra: la media dei 14 giocatori migliori (gli undici titolari e qualche riserva) */
    public double forzaMedia() {
        return giocatori.stream()
                .map(GiocatoreGenerato::valutazione)
                .sorted(Comparator.reverseOrder())
                .limit(14)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);
    }
}
