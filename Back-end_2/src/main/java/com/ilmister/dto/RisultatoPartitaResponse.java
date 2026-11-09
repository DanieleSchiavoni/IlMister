package com.ilmister.dto;

/** Il risultato di una partita; "dellUtente" è vero se gioca la squadra allenata dall'utente */
public record RisultatoPartitaResponse(Long partitaId, String squadraCasa, String squadraTrasferta, int golCasa,
        int golTrasferta, boolean dellUtente) {
}
