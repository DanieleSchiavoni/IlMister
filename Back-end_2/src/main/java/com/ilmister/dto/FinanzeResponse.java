package com.ilmister.dto;

import java.math.BigDecimal;
import java.util.List;

/** La situazione economica della squadra dell'utente */
public record FinanzeResponse(BigDecimal budget, BigDecimal monteStipendiAnnuo, BigDecimal valoreRosa, int giocatori,
        BigDecimal entrateTotali, BigDecimal usciteTotali, List<MovimentoResponse> movimenti) {
}
