package com.ilmister.dto;

/** Una riga della classifica; "dellUtente" è vero per la squadra allenata dall'utente */
public record RigaClassificaResponse(int posizione, Long squadraId, String squadra, int giocate, int vittorie,
        int pareggi, int sconfitte, int golFatti, int golSubiti, int differenzaReti, int punti, boolean dellUtente) {
}
