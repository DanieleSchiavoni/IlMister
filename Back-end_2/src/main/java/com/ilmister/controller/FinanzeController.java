package com.ilmister.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ilmister.dto.FinanzeResponse;
import com.ilmister.security.UtenteAutenticato;
import com.ilmister.service.FinanzeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/carriere/{carrieraId}/finanze")
@RequiredArgsConstructor
@Tag(name = "Finanze")
public class FinanzeController {

    private final FinanzeService finanzeService;

    @GetMapping
    @Operation(summary = "Budget, monte stipendi, valore della rosa e ultimi movimenti")
    public FinanzeResponse finanze(@AuthenticationPrincipal UtenteAutenticato utente, @PathVariable Long carrieraId) {
        return finanzeService.finanze(carrieraId, utente.id());
    }
}
