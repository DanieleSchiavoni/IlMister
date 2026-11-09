package com.ilmister.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Un giocatore che si può trattare: di un'altra squadra (squadraId pieno) o svincolato (squadraId nullo) */
public record GiocatoreMercatoResponse(Long id, String nome, String cognome, String siglaPosizione, String ruolo,
        int eta, int valutazione, int potenziale, BigDecimal valoreMercato, BigDecimal stipendioAnnuo,
        LocalDate scadenzaContratto, Long squadraId, String squadra) {
}
