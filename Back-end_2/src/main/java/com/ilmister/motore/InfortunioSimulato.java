package com.ilmister.motore;

/** Un infortunio avvenuto in partita: quanti giorni di stop e di che tipo */
public record InfortunioSimulato(long giocatoreId, Lato lato, int giorni, String tipo) {
}
