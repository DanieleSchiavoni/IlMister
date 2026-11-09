package com.ilmister.motore;

import java.util.List;

/**
 * Ciò che l'allenatore decide all'intervallo.
 *
 * @param istruzioni               le nuove istruzioni tattiche, oppure nulle per lasciare quelle di prima
 * @param sostituzioni             i cambi da fare subito (al massimo cinque in tutta la partita)
 * @param sostituzioniAutomatiche  se vero, nel secondo tempo i giocatori stanchi vengono sostituiti da soli
 * @param adattaMentalita          se vero, la squadra diventa più offensiva se perde e più prudente se vince
 */
public record DecisioniIntervallo(Istruzioni istruzioni, List<Sostituzione> sostituzioni,
        boolean sostituzioniAutomatiche, boolean adattaMentalita) {

    public DecisioniIntervallo {
        sostituzioni = sostituzioni == null ? List.of() : List.copyOf(sostituzioni);
    }

    /** Per le squadre guidate dal computer */
    public static DecisioniIntervallo automatiche() {
        return new DecisioniIntervallo(null, List.of(), true, true);
    }

    /** Per la squadra dell'utente: decide lui le istruzioni e i cambi, i giocatori stanchi sono sostituiti da soli */
    public static DecisioniIntervallo manuali(Istruzioni istruzioni, List<Sostituzione> sostituzioni) {
        return new DecisioniIntervallo(istruzioni, sostituzioni, true, false);
    }
}
