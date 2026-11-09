package com.ilmister.motore;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ilmister.entity.enumerazioni.Modulo;

/**
 * Una squadra pronta a giocare: i titolari sono nell'ordine dei posti del modulo (il primo è il portiere).
 */
public record SquadraInCampo(long id, String nome, Modulo modulo, Istruzioni istruzioni,
        List<GiocatoreInCampo> titolari, List<GiocatoreInCampo> panchina) {

    public SquadraInCampo {
        if (titolari == null || titolari.size() != Modulo.TITOLARI) {
            throw new IllegalArgumentException("Servono esattamente " + Modulo.TITOLARI + " titolari");
        }
        titolari = List.copyOf(titolari);
        panchina = panchina == null ? List.of() : List.copyOf(panchina);
        Set<Long> identificativi = new HashSet<>();
        for (GiocatoreInCampo giocatore : titolari) {
            if (!identificativi.add(giocatore.id())) {
                throw new IllegalArgumentException("Un giocatore non può essere schierato due volte");
            }
        }
        for (GiocatoreInCampo giocatore : panchina) {
            if (!identificativi.add(giocatore.id())) {
                throw new IllegalArgumentException("Un giocatore non può essere schierato due volte");
            }
        }
    }
}
