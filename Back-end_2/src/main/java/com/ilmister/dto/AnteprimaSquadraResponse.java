package com.ilmister.dto;

import java.util.List;

public record AnteprimaSquadraResponse(int indice, String nome, String citta, String stadio, int capienza,
        int reputazione, double forzaMedia, List<AnteprimaGiocatoreResponse> migliori) {
}
