package com.ilmister.security;

import com.ilmister.entity.enumerazioni.RuoloUtente;

/** L'utente che sta facendo la richiesta, ricavato dal token JWT; nei controller si riceve con @AuthenticationPrincipal */
public record UtenteAutenticato(Long id, String username, RuoloUtente ruolo) {
}
