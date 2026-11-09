package com.ilmister.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ilmister.dto.ImpostaAbilitatoRichiesta;
import com.ilmister.dto.UtenteResponse;
import com.ilmister.security.UtenteAutenticato;
import com.ilmister.service.UtenteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/utenti")
@RequiredArgsConstructor
@Tag(name = "Amministrazione", description = "Solo per gli utenti con ruolo ADMIN (altrimenti 403)")
public class AdminController {

    private final UtenteService utenteService;

    @GetMapping
    @Operation(summary = "Elenco degli utenti")
    public List<UtenteResponse> elenco() {
        return utenteService.elenco();
    }

    @PatchMapping("/{utenteId}/abilitato")
    @Operation(summary = "Blocca o sblocca un utente", description = "Bloccandolo si chiudono le sue sessioni")
    public UtenteResponse impostaAbilitato(@AuthenticationPrincipal UtenteAutenticato admin,
            @PathVariable Long utenteId, @Valid @RequestBody ImpostaAbilitatoRichiesta richiesta) {
        return utenteService.impostaAbilitato(admin.id(), utenteId, richiesta.abilitato());
    }
}
