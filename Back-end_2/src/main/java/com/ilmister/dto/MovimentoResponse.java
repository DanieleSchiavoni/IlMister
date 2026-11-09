package com.ilmister.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.ilmister.entity.enumerazioni.TipoMovimento;

public record MovimentoResponse(Long id, LocalDate data, TipoMovimento tipo, BigDecimal importo, String descrizione) {
}
