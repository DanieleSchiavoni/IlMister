package com.ilmister.dto;

import java.time.LocalDate;
import java.util.List;

public record GiornataResponse(int numero, LocalDate data, List<PartitaInCalendarioResponse> partite) {
}
