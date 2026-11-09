package com.ilmister.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ilmister.dto.UtenteResponse;
import com.ilmister.security.UtenteAutenticato;
import com.ilmister.service.UtenteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/utenti")
@RequiredArgsConstructor
@Tag(name = "Utente")
public class UtenteController {

    private final UtenteService utenteService;

    @GetMapping("/me")
    @Operation(summary = "Il profilo dell'utente che ha fatto la richiesta")
    public UtenteResponse profilo(@AuthenticationPrincipal UtenteAutenticato utente) {
        return utenteService.profilo(utente.id());
    }
}
