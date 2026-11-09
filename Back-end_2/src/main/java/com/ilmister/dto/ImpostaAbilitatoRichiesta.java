package com.ilmister.dto;

import jakarta.validation.constraints.NotNull;

public record ImpostaAbilitatoRichiesta(@NotNull(message = "Il valore è obbligatorio") Boolean abilitato) {
}
