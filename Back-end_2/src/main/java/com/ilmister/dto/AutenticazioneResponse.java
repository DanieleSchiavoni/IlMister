package com.ilmister.dto;

/**
 * I token consegnati dopo login e rinnovo. L'accessToken (breve) si manda in ogni richiesta; il refreshToken
 * (lungo, monouso) serve solo a ottenere una nuova coppia di token.
 */
public record AutenticazioneResponse(String accessToken, String refreshToken, String tipo, long scadenzaAccessInSecondi,
        UtenteResponse utente) {
}
