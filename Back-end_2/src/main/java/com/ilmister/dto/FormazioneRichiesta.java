package com.ilmister.dto;

import java.util.List;

import com.ilmister.entity.enumerazioni.LineaDifensiva;
import com.ilmister.entity.enumerazioni.Mentalita;
import com.ilmister.entity.enumerazioni.Modulo;
import com.ilmister.entity.enumerazioni.Pressing;
import com.ilmister.entity.enumerazioni.Ritmo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * La formazione scelta dall'allenatore. I titolari sono nell'ordine dei posti del modulo
 * (il primo è il portiere); la panchina ha al massimo 7 giocatori.
 */
public record FormazioneRichiesta(
        @NotNull(message = "Il modulo è obbligatorio") Modulo modulo,
        @NotNull(message = "La mentalità è obbligatoria") Mentalita mentalita,
        @NotNull(message = "Il pressing è obbligatorio") Pressing pressing,
        @NotNull(message = "Il ritmo è obbligatorio") Ritmo ritmo,
        @NotNull(message = "La linea difensiva è obbligatoria") LineaDifensiva lineaDifensiva,
        @NotNull(message = "I titolari sono obbligatori")
        @Size(min = 11, max = 11, message = "I titolari devono essere esattamente 11")
        List<@NotNull(message = "Titolare non valido") Long> titolari,
        @NotNull(message = "La panchina è obbligatoria")
        @Size(max = 7, message = "La panchina può avere al massimo 7 giocatori")
        List<@NotNull(message = "Giocatore in panchina non valido") Long> panchina) {
}
