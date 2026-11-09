package com.ilmister.dto;

import jakarta.validation.constraints.NotNull;

/** Un cambio: esce un giocatore in campo, entra uno della panchina */
public record SostituzioneRichiesta(
        @NotNull(message = "Il giocatore che esce è obbligatorio") Long esceId,
        @NotNull(message = "Il giocatore che entra è obbligatorio") Long entraId) {
}
