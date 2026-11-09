package com.ilmister.dto;

import java.math.BigDecimal;

/** Com'è andata una trattativa: "conclusa" è falso se la controparte ha rifiutato (non è un errore) */
public record EsitoTrattativaResponse(boolean conclusa, String messaggio, BigDecimal budgetResiduo) {
}
