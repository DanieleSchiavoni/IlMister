package com.ilmister.dto;

import com.ilmister.entity.enumerazioni.FocusAllenamento;

import jakarta.validation.constraints.NotNull;

public record ImpostaFocusRichiesta(@NotNull(message = "Il focus è obbligatorio") FocusAllenamento focus) {
}
