package com.ilmister.dto;

import java.time.LocalDate;

import com.ilmister.generazione.Attributi;

/** Un giocatore con attributi e stato del momento; "disponibile" è falso se infortunato o squalificato */
public record GiocatoreResponse(Long id, String nome, String cognome, String posizione, String siglaPosizione,
        String ruolo, Integer numeroMaglia, int eta, String nazionalita, String piede, int valutazione,
        int potenziale, Attributi attributi, int forma, int morale, int energia, boolean infortunato,
        String tipoInfortunio, LocalDate infortunatoFinoAl, int squalificaGiornate, boolean disponibile) {
}
