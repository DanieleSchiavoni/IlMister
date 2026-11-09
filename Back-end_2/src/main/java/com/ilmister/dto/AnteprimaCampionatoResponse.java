package com.ilmister.dto;

import java.util.List;

public record AnteprimaCampionatoResponse(String nome, long seme, List<AnteprimaSquadraResponse> squadre) {
}
