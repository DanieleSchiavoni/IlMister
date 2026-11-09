package com.ilmister.generazione;

import java.util.List;

/** Un campionato con le sue squadre e i loro giocatori, generato o importato da un file */
public record CampionatoGenerato(String nome, int livello, long seme, List<SquadraGenerata> squadre) {
}
