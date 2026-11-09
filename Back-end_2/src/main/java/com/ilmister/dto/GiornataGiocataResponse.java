package com.ilmister.dto;

import java.time.LocalDate;
import java.util.List;

/** Ciò che è successo avanzando di una giornata */
public record GiornataGiocataResponse(int numero, LocalDate data, List<RisultatoPartitaResponse> risultati,
        boolean stagioneConclusa) {
}
