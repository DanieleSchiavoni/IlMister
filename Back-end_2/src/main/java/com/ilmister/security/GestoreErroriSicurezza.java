package com.ilmister.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilmister.dto.ErroreResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Gli errori di sicurezza nascono nei filtri, prima dei controller, e quindi non passano dal gestore delle
 * eccezioni: qui escono con la stessa forma JSON. 401 se manca l'accesso, 403 se manca il permesso.
 */
public class GestoreErroriSicurezza implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public GestoreErroriSicurezza(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest richiesta, HttpServletResponse risposta,
            AuthenticationException eccezione) throws IOException {
        scrivi(richiesta, risposta, HttpStatus.UNAUTHORIZED, "NON_AUTENTICATO", "Accesso richiesto: effettua il login");
    }

    @Override
    public void handle(HttpServletRequest richiesta, HttpServletResponse risposta,
            AccessDeniedException eccezione) throws IOException {
        scrivi(richiesta, risposta, HttpStatus.FORBIDDEN, "ACCESSO_NEGATO", "Non hai i permessi per questa operazione");
    }

    private void scrivi(HttpServletRequest richiesta, HttpServletResponse risposta, HttpStatus stato, String errore,
            String messaggio) throws IOException {
        risposta.setStatus(stato.value());
        risposta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        risposta.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(risposta.getWriter(),
                ErroreResponse.di(stato.value(), errore, messaggio, richiesta.getRequestURI()));
    }
}
