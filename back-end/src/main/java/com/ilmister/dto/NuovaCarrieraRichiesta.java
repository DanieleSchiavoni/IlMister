package com.ilmister.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * I dati per iniziare una carriera. Il campionato nasce dal seme: lo stesso seme dell'anteprima da cui
 * l'utente ha scelto la squadra, così il campionato creato è quello che ha visto.
 */
public record NuovaCarrieraRichiesta(
        @NotBlank(message = "Il nome della carriera è obbligatorio")
        @Size(max = 80, message = "Il nome della carriera può avere al massimo 80 caratteri")
        String nome,

        @NotBlank(message = "Il nome dell'allenatore è obbligatorio")
        @Size(max = 80, message = "Il nome dell'allenatore può avere al massimo 80 caratteri")
        String nomeAllenatore,

        @NotBlank(message = "La squadra è obbligatoria")
        @Size(max = 80, message = "Il nome della squadra può avere al massimo 80 caratteri")
        String nomeSquadra,

        @NotNull(message = "Il seme del campionato è obbligatorio")
        Long seme) {
}
