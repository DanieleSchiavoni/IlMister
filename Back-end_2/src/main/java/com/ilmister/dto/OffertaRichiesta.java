package com.ilmister.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

/** Un'offerta per un giocatore di un'altra squadra */
public record OffertaRichiesta(
        @NotNull(message = "Il giocatore è obbligatorio") Long giocatoreId,
        @NotNull(message = "L'importo è obbligatorio")
        @DecimalMin(value = "1", message = "L'importo deve essere positivo")
        @Digits(integer = 12, fraction = 2, message = "L'importo non è valido") BigDecimal importo) {
}
