package com.ilmister.dto;

import jakarta.validation.constraints.NotNull;

/** Una richiesta che riguarda un solo giocatore (vendita, svincolo, ingaggio) */
public record GiocatoreIdRichiesta(@NotNull(message = "Il giocatore è obbligatorio") Long giocatoreId) {
}
