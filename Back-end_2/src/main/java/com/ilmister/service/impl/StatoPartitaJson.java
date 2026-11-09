package com.ilmister.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilmister.motore.DatiPartita;

/**
 * Salva e rilegge in JSON le squadre schierate (formazioni, giocatori e stato fisico) di una partita ferma
 * all'intervallo. Il motore è deterministico: con questi dati e il seme della partita il primo tempo si rigioca
 * identico, poi si prosegue con le decisioni dell'allenatore.
 */
final class StatoPartitaJson {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private StatoPartitaJson() {
    }

    static String scrivi(DatiPartita dati) {
        try {
            return MAPPER.writeValueAsString(dati);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Impossibile salvare lo stato della partita", e);
        }
    }

    static DatiPartita leggi(String json) {
        try {
            return MAPPER.readValue(json, DatiPartita.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Impossibile leggere lo stato della partita", e);
        }
    }
}
