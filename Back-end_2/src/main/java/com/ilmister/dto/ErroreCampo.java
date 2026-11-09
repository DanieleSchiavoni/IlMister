package com.ilmister.dto;

/** Un campo della richiesta che non ha superato la validazione */
public record ErroreCampo(String campo, String messaggio) {
}
