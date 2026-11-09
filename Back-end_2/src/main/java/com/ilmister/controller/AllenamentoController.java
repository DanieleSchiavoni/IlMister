package com.ilmister.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ilmister.dto.AllenamentoResponse;
import com.ilmister.dto.ImpostaFocusRichiesta;
import com.ilmister.security.UtenteAutenticato;
import com.ilmister.service.AllenamentoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/carriere/{carrieraId}/allenamento")
@RequiredArgsConstructor
@Tag(name = "Allenamento")
public class AllenamentoController {

    private final AllenamentoService allenamentoService;

    @GetMapping
    @Operation(summary = "Il focus di allenamento attuale e quelli disponibili")
    public AllenamentoResponse allenamento(@AuthenticationPrincipal UtenteAutenticato utente,
            @PathVariable Long carrieraId) {
        return allenamentoService.allenamento(carrieraId, utente.id());
    }

    @PutMapping
    @Operation(summary = "Cambia il focus di allenamento",
            description = "A ogni giornata i giocatori della tua squadra migliorano un po' gli attributi del focus")
    public AllenamentoResponse impostaFocus(@AuthenticationPrincipal UtenteAutenticato utente,
            @PathVariable Long carrieraId, @Valid @RequestBody ImpostaFocusRichiesta richiesta) {
        return allenamentoService.impostaFocus(carrieraId, utente.id(), richiesta.focus());
    }
}
