package com.ilmister.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ilmister.dto.FormazioneRichiesta;
import com.ilmister.dto.FormazioneResponse;
import com.ilmister.security.UtenteAutenticato;
import com.ilmister.service.FormazioneService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/carriere/{carrieraId}/formazione")
@RequiredArgsConstructor
@Tag(name = "Formazione")
public class FormazioneController {

    private final FormazioneService formazioneService;

    @GetMapping
    @Operation(summary = "La formazione salvata o, se manca, la proposta automatica")
    public FormazioneResponse formazione(@AuthenticationPrincipal UtenteAutenticato utente,
            @PathVariable Long carrieraId) {
        return formazioneService.formazione(carrieraId, utente.id());
    }

    @PutMapping
    @Operation(summary = "Salva la formazione",
            description = "11 titolari nell'ordine dei posti del modulo (il primo è il portiere) e al massimo 7 in panchina. "
                    + "400 se un giocatore è doppio, di un'altra squadra o non disponibile.")
    public FormazioneResponse salva(@AuthenticationPrincipal UtenteAutenticato utente, @PathVariable Long carrieraId,
            @Valid @RequestBody FormazioneRichiesta richiesta) {
        return formazioneService.salva(carrieraId, utente.id(), richiesta);
    }
}
