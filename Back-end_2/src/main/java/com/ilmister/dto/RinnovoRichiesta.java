package com.ilmister.dto;

import jakarta.validation.constraints.NotBlank;

public record RinnovoRichiesta(@NotBlank(message = "Il refresh token è obbligatorio") String refreshToken) {
}
