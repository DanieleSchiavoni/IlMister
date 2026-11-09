package com.ilmister.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistrazioneRichiesta(
        @NotBlank(message = "Lo username è obbligatorio")
        @Size(min = 3, max = 50, message = "Lo username deve avere da 3 a 50 caratteri")
        @Pattern(regexp = "^[A-Za-z0-9_.-]*$", message = "Lo username può contenere solo lettere, numeri, punto, trattino e underscore")
        String username,

        @NotBlank(message = "L'email è obbligatoria")
        @Email(message = "L'email non è valida")
        @Size(max = 120, message = "L'email può avere al massimo 120 caratteri")
        String email,

        // BCrypt considera solo i primi 72 byte: oltre non avrebbe senso accettarli
        @NotBlank(message = "La password è obbligatoria")
        @Size(min = 8, max = 72, message = "La password deve avere da 8 a 72 caratteri")
        String password) {
}
