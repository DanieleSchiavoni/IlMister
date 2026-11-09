package com.ilmister.motore;

import com.ilmister.entity.enumerazioni.LineaDifensiva;
import com.ilmister.entity.enumerazioni.Mentalita;
import com.ilmister.entity.enumerazioni.Pressing;
import com.ilmister.entity.enumerazioni.Ritmo;

/** Le istruzioni tattiche di una squadra */
public record Istruzioni(Mentalita mentalita, Pressing pressing, Ritmo ritmo, LineaDifensiva lineaDifensiva) {

    public Istruzioni {
        if (mentalita == null || pressing == null || ritmo == null || lineaDifensiva == null) {
            throw new IllegalArgumentException("Le istruzioni tattiche devono essere tutte indicate");
        }
    }

    public static Istruzioni equilibrate() {
        return new Istruzioni(Mentalita.EQUILIBRATA, Pressing.MEDIO, Ritmo.NORMALE, LineaDifensiva.MEDIA);
    }

    public Istruzioni conMentalita(Mentalita nuova) {
        return new Istruzioni(nuova, pressing, ritmo, lineaDifensiva);
    }
}
