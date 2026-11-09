package com.ilmister.motore;

import com.ilmister.entity.enumerazioni.Posizione;
import com.ilmister.generazione.Attributi;

/**
 * Un giocatore come lo vede il motore: solo ciò che serve a giocare la partita.
 *
 * @param energia da 0 a 100: la freschezza con cui entra in campo
 * @param forma   da 1 a 100: il momento di forma
 * @param morale  da 1 a 100
 */
public record GiocatoreInCampo(long id, String cognome, Posizione posizione, Attributi attributi, int forma, int morale,
        int energia, int numeroMaglia) {
}
