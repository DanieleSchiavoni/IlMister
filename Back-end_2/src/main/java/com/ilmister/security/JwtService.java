package com.ilmister.security;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ilmister.entity.Utente;
import com.ilmister.entity.enumerazioni.RuoloUtente;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/** Crea e legge gli access token: JWT firmati con HMAC-SHA256, di breve durata */
@Component
public class JwtService {

    static final int LUNGHEZZA_MINIMA_SEGRETO = 32;

    private final SecretKey chiave;
    private final Duration durata;
    private final Clock clock;

    public JwtService(@Value("${app.jwt.secret}") String segreto,
            @Value("${app.jwt.durata-access-minuti:15}") long durataMinuti, Clock clock) {
        byte[] byteSegreto = segreto == null ? new byte[0] : segreto.getBytes(StandardCharsets.UTF_8);
        if (byteSegreto.length < LUNGHEZZA_MINIMA_SEGRETO) {
            throw new IllegalStateException("Il segreto JWT deve avere almeno " + LUNGHEZZA_MINIMA_SEGRETO
                    + " caratteri: impostare la variabile d'ambiente JWT_SECRET");
        }
        this.chiave = Keys.hmacShaKeyFor(byteSegreto);
        this.durata = Duration.ofMinutes(durataMinuti);
        this.clock = clock;
    }

    public long durataInSecondi() {
        return durata.toSeconds();
    }

    public String genera(Utente utente) {
        Instant ora = clock.instant();
        return Jwts.builder()
                .subject(String.valueOf(utente.getId()))
                .claim("username", utente.getUsername())
                .claim("ruolo", utente.getRuolo().name())
                .issuedAt(Date.from(ora))
                .expiration(Date.from(ora.plus(durata)))
                .signWith(chiave)
                .compact();
    }

    /** L'utente indicato dal token; vuoto se il token è scaduto, manomesso o malformato */
    public Optional<UtenteAutenticato> leggi(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(chiave)
                    .clock(() -> Date.from(clock.instant()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(new UtenteAutenticato(Long.valueOf(claims.getSubject()),
                    claims.get("username", String.class), RuoloUtente.valueOf(claims.get("ruolo", String.class))));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
