package com.ilmister.dto;

import java.util.List;

public record AnteprimaRosaResponse(String squadra, List<AnteprimaGiocatoreResponse> giocatori) {
}
