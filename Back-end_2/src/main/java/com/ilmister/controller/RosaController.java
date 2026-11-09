package com.ilmister.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ilmister.dto.GiocatoreResponse;
import com.ilmister.security.UtenteAutenticato;
import com.ilmister.service.RosaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/carriere/{carrieraId}")
@RequiredArgsConstructor
@Tag(name = "Rosa")
public class RosaController {

    private final RosaService rosaService;

    @GetMapping("/rosa")
    @Operation(summary = "La rosa di una squadra", description = "Senza squadraId, la rosa della squadra dell'utente")
    public List<GiocatoreResponse> rosa(@AuthenticationPrincipal UtenteAutenticato utente,
            @PathVariable Long carrieraId, @RequestParam(required = false) Long squadraId) {
        return rosaService.rosa(carrieraId, utente.id(), squadraId);
    }

    @GetMapping("/giocatori/{giocatoreId}")
    @Operation(summary = "Un giocatore della carriera, con attributi e stato")
    public GiocatoreResponse giocatore(@AuthenticationPrincipal UtenteAutenticato utente,
            @PathVariable Long carrieraId, @PathVariable Long giocatoreId) {
        return rosaService.giocatore(carrieraId, utente.id(), giocatoreId);
    }
}
