package com.ilmister.dto;

import java.time.LocalDateTime;

import com.ilmister.entity.enumerazioni.RuoloUtente;

/** I dati pubblici di un utente: la password non esce mai */
public record UtenteResponse(Long id, String username, String email, RuoloUtente ruolo, boolean abilitato,
        LocalDateTime dataCreazione) {
}
