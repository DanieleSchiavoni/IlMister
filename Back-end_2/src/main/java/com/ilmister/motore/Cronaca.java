package com.ilmister.motore;

import java.util.SplittableRandom;

/** Le frasi della cronaca: per ogni evento più varianti, scelte a caso (ma sempre le stesse con lo stesso seme) */
final class Cronaca {

    private Cronaca() {
    }

    private static String scegli(SplittableRandom r, String... modelli) {
        return modelli[r.nextInt(modelli.length)];
    }

    static String inizio(String casa, String trasferta, String stadio) {
        return "Fischio d'inizio! %s - %s (%s).".formatted(casa, trasferta, stadio);
    }

    static String intervallo(String casa, int golCasa, int golTrasferta, String trasferta) {
        return "Fine del primo tempo: %s %d - %d %s.".formatted(casa, golCasa, golTrasferta, trasferta);
    }

    static String inizioSecondoTempo() {
        return "Si riparte! Comincia la ripresa.";
    }

    static String fine(String casa, int golCasa, int golTrasferta, String trasferta) {
        return "Triplice fischio: %s %d - %d %s.".formatted(casa, golCasa, golTrasferta, trasferta);
    }

    static String gol(SplittableRandom r, String marcatore, String assist, String squadra, boolean rigore) {
        if (rigore) {
            return scegli(r,
                    "GOL! %s dal dischetto spiazza il portiere: %s in vantaggio!",
                    "GOL! %s non sbaglia dagli undici metri, rete per il %s!",
                    "GOL! Rigore trasformato con freddezza da %s (%s).").formatted(marcatore, squadra);
        }
        if (assist != null) {
            return scegli(r,
                    "GOL! %1$s (%3$s) trova la rete su assist di %2$s!",
                    "GOL! Bella azione di %3$s: %2$s serve %1$s che non sbaglia!",
                    "GOL! %2$s mette in mezzo, %1$s la spinge in rete per il %3$s!",
                    "GOL! Triangolazione perfetta, %1$s batte il portiere su suggerimento di %2$s!")
                    .formatted(marcatore, assist, squadra);
        }
        return scegli(r,
                "GOL! %1$s (%2$s) con una conclusione imparabile!",
                "GOL! %1$s si inventa la rete da solo, esulta il %2$s!",
                "GOL! Tiro preciso di %1$s e il pallone si infila in rete per il %2$s!",
                "GOL! %1$s approfitta di un errore della difesa e segna per il %2$s!")
                .formatted(marcatore, squadra);
    }

    static String tiroParato(SplittableRandom r, String tiratore, String portiere) {
        return scegli(r,
                "Tiro di %s, ma %s è attento e respinge.",
                "%s ci prova da posizione invitante: grande intervento di %s!",
                "Conclusione di %s, ma %s si distende e para.",
                "%s calcia forte, %s blocca senza problemi.").formatted(tiratore, portiere);
    }

    static String tiroFuori(SplittableRandom r, String tiratore) {
        return scegli(r,
                "%s calcia dalla distanza, ma il pallone esce di poco.",
                "Occasione per %s, che però manda alto sopra la traversa.",
                "%s ci prova, ma la mira non è quella giusta.",
                "Tiro di %s: il pallone sfiora il palo e finisce fuori.").formatted(tiratore);
    }

    static String tiroBloccato(SplittableRandom r, String tiratore) {
        return scegli(r,
                "%s tira, ma la difesa si frappone e salva.",
                "Conclusione di %s murata da un difensore.",
                "%s prova la conclusione, ma c'è un muro davanti a lui.").formatted(tiratore);
    }

    static String palo(SplittableRandom r, String tiratore) {
        return scegli(r,
                "PALO! %s colpisce il montante, che fortuna per la difesa!",
                "Sfiorato il gol! Il tiro di %s si stampa sul palo!",
                "Traversa! %s ha il gol sul piede, ma il legno lo ferma!").formatted(tiratore);
    }

    static String occasione(SplittableRandom r, String giocatore, String squadra) {
        return scegli(r,
                "Azione pericolosa del %2$s: %1$s ci prova, ma la difesa chiude.",
                "%1$s si libera sulla fascia, ma l'ultimo passaggio non arriva.",
                "Brivido per la difesa: %1$s sfiora la deviazione vincente.",
                "Bella giocata di %1$s, il %2$s costruisce ma non conclude.").formatted(giocatore, squadra);
    }

    static String angolo(SplittableRandom r, String squadra) {
        return scegli(r,
                "Calcio d'angolo per il %s.",
                "Corner per il %s: palla in mezzo all'area.").formatted(squadra);
    }

    static String rigoreAssegnato(SplittableRandom r, String squadra) {
        return scegli(r,
                "RIGORE per il %s! L'arbitro indica il dischetto dopo un fallo in area.",
                "L'arbitro fischia: calcio di rigore a favore del %s!").formatted(squadra);
    }

    static String fallo(SplittableRandom r, String giocatore) {
        return scegli(r,
                "Fallo di %s, l'arbitro ferma il gioco.",
                "%s interviene in ritardo e viene fermato dall'arbitro.").formatted(giocatore);
    }

    static String ammonizione(SplittableRandom r, String giocatore, String squadra) {
        return scegli(r,
                "Cartellino giallo per %s (%s) dopo un intervento duro.",
                "%s (%s) entra in ritardo e finisce sul taccuino dell'arbitro.",
                "Ammonito %s (%s): proteste e giallo.").formatted(giocatore, squadra);
    }

    static String espulsione(SplittableRandom r, String giocatore, String squadra, boolean doppiaAmmonizione) {
        if (doppiaAmmonizione) {
            return scegli(r,
                    "ESPULSO %s (%s)! Secondo giallo, il %s resta in dieci.",
                    "Doppia ammonizione per %s (%s): rosso e il %s è in inferiorità numerica.")
                    .formatted(giocatore, squadra, squadra);
        }
        return scegli(r,
                "ROSSO DIRETTO per %s (%s)! Il %s resta in dieci.",
                "Che errore di %s (%s): l'arbitro estrae il rosso, il %s gioca in dieci.")
                .formatted(giocatore, squadra, squadra);
    }

    static String infortunio(SplittableRandom r, String giocatore, String squadra) {
        return scegli(r,
                "Brutto momento per il %2$s: %1$s si ferma e si tocca la gamba.",
                "%1$s (%2$s) resta a terra, serve l'intervento dello staff medico.",
                "Problema fisico per %1$s: il %2$s è costretto a pensare a un cambio.").formatted(giocatore, squadra);
    }

    static String sostituzione(SplittableRandom r, String squadra, String esce, String entra, boolean perInfortunio) {
        if (perInfortunio) {
            return "Cambio forzato per il %s: esce %s, entra %s.".formatted(squadra, esce, entra);
        }
        return scegli(r,
                "Cambio per il %s: esce %s, entra %s.",
                "Sostituzione nel %s: %s lascia il campo, dentro %s.").formatted(squadra, esce, entra);
    }
}
