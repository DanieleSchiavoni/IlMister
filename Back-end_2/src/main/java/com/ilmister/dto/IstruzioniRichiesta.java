package com.ilmister.dto;

import com.ilmister.entity.enumerazioni.LineaDifensiva;
import com.ilmister.entity.enumerazioni.Mentalita;
import com.ilmister.entity.enumerazioni.Pressing;
import com.ilmister.entity.enumerazioni.Ritmo;

import jakarta.validation.constraints.NotNull;

public record IstruzioniRichiesta(
        @NotNull(message = "La mentalità è obbligatoria") Mentalita mentalita,
        @NotNull(message = "Il pressing è obbligatorio") Pressing pressing,
        @NotNull(message = "Il ritmo è obbligatorio") Ritmo ritmo,
        @NotNull(message = "La linea difensiva è obbligatoria") LineaDifensiva lineaDifensiva) {
}
