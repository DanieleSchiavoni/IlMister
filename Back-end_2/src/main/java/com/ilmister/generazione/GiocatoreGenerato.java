package com.ilmister.generazione;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.ilmister.entity.enumerazioni.Piede;
import com.ilmister.entity.enumerazioni.Posizione;

/** Un giocatore pronto per essere salvato: lo stesso formato serve a quelli generati e a quelli importati */
public record GiocatoreGenerato(String nome, String cognome, LocalDate dataNascita, Posizione posizione, Piede piede,
        int numeroMaglia, Attributi attributi, int valutazione, int potenziale, BigDecimal stipendioAnnuo,
        LocalDate scadenzaContratto, BigDecimal valoreMercato) {
}
