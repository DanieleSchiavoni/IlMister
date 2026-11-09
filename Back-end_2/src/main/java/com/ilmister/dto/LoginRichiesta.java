package com.ilmister.dto;

import jakarta.validation.constraints.NotBlank;

/** L'accesso si fa con lo username oppure con l'email */
public record LoginRichiesta(
        @NotBlank(message = "Username o email obbligatori") String identificativo,
        @NotBlank(message = "La password è obbligatoria") String password) {
}
