package com.ilmister.motore;

import org.springframework.stereotype.Component;

/**
 * Il motore di simulazione delle partite. È una funzione pura: non legge né scrive il database, e con gli stessi
 * dati e lo stesso seme dà sempre la stessa partita.
 *
 * Una partita si gioca a due tempi, perché all'intervallo l'allenatore può cambiare tattica e giocatori:
 * {@link #primoTempo} mostra il primo tempo; {@link #giocaPartita} la gioca tutta con le decisioni prese
 * all'intervallo. Il primo tempo di giocaPartita è identico a quello già mostrato da primoTempo.
 */
@Component
public class MotorePartita {

    public RisultatoPrimoTempo primoTempo(DatiPartita dati) {
        Simulazione simulazione = new Simulazione(dati);
        simulazione.giocaPrimoTempo();
        return simulazione.risultatoPrimoTempo();
    }

    /**
     * Gioca la partita intera.
     *
     * @param decisioniCasa       le scelte dell'intervallo per la squadra di casa; nulle per quelle del computer
     * @param decisioniTrasferta  le scelte dell'intervallo per la squadra ospite; nulle per quelle del computer
     * @throws IllegalArgumentException se un cambio non è valido (giocatore non in campo, non in panchina, più di cinque)
     */
    public RisultatoPartita giocaPartita(DatiPartita dati, DecisioniIntervallo decisioniCasa,
            DecisioniIntervallo decisioniTrasferta) {
        Simulazione simulazione = new Simulazione(dati);
        simulazione.giocaPrimoTempo();
        simulazione.applicaDecisioni(decisioniCasa, decisioniTrasferta);
        simulazione.giocaSecondoTempo();
        return simulazione.risultatoFinale();
    }

    /** Una partita tra due squadre del computer */
    public RisultatoPartita giocaPartita(DatiPartita dati) {
        return giocaPartita(dati, null, null);
    }
}
