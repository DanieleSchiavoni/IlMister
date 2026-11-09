package com.ilmister.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Legge l'intestazione "Authorization: Bearer ..." e, se il token è valido, identifica l'utente.
 * Con un token assente o non valido non fa nulla: sarà poi la catena di sicurezza a rispondere 401
 * sulle richieste che richiedono l'accesso.
 */
public class FiltroJwt extends OncePerRequestFilter {

    private static final String PREFISSO = "Bearer ";

    private final JwtService jwtService;

    public FiltroJwt(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest richiesta, HttpServletResponse risposta, FilterChain catena)
            throws ServletException, IOException {
        String intestazione = richiesta.getHeader("Authorization");
        if (intestazione != null && intestazione.startsWith(PREFISSO)) {
            jwtService.leggi(intestazione.substring(PREFISSO.length()).trim()).ifPresent(utente -> {
                var autenticazione = new UsernamePasswordAuthenticationToken(utente, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + utente.ruolo().name())));
                SecurityContextHolder.getContext().setAuthentication(autenticazione);
            });
        }
        catena.doFilter(richiesta, risposta);
    }
}
