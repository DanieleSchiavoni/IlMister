package com.ilmister.motore;

/** Le statistiche di una squadra nella partita; l'xG è la somma delle probabilità di gol delle conclusioni */
public record StatisticheSquadra(int possesso, int tiri, int tiriInPorta, double xg, int falli, int calciDAngolo,
        int ammonizioni, int espulsioni) {
}
