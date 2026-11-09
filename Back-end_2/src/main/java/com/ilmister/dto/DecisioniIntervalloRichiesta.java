package com.ilmister.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

/**
 * Le scelte dell'allenatore all'intervallo. Le istruzioni, se nulle, restano quelle di prima;
 * i cambi possono essere al massimo cinque.
 */
public record DecisioniIntervalloRichiesta(
        @Valid IstruzioniRichiesta istruzioni,
        @Size(max = 5, message = "Si possono fare al massimo 5 cambi")
        List<@Valid SostituzioneRichiesta> sostituzioni) {
}
