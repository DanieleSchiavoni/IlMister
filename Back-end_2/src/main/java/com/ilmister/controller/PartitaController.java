package com.ilmister.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ilmister.dto.DecisioniIntervalloRichiesta;
import com.ilmister.dto.GiornataGiocataResponse;
import com.ilmister.dto.PartitaDettaglioResponse;
import com.ilmister.dto.PrimoTempoResponse;
import com.ilmister.security.UtenteAutenticato;
import com.ilmister.service.AvanzamentoService;
import com.ilmister.service.PartitaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/carriere/{carrieraId}")
@RequiredArgsConstructor
@Tag(name = "Partite", description = "Si gioca la giornata di colpo oppure a due tempi, con l'intervallo")
public class PartitaController {

    private final AvanzamentoService avanzamentoService;
    private final PartitaService partitaService;

    @PostMapping("/giornata/avanza")
    @Operation(summary = "Gioca la prossima giornata di colpo",
            description = "La partita dell'utente usa la sua formazione salvata (o quella automatica)")
    public GiornataGiocataResponse avanza(@AuthenticationPrincipal UtenteAutenticato utente,
            @PathVariable Long carrieraId) {
        return avanzamentoService.avanza(carrieraId, utente.id());
    }

    @PostMapping("/partita/primo-tempo")
    @Operation(summary = "Gioca il primo tempo della partita dell'utente e si ferma all'intervallo",
            description = "Se la partita è già all'intervallo ne ripropone il primo tempo")
    public PrimoTempoResponse primoTempo(@AuthenticationPrincipal UtenteAutenticato utente,
            @PathVariable Long carrieraId) {
        return avanzamentoService.giocaPrimoTempo(carrieraId, utente.id());
    }

    @PostMapping("/partita/conclusione")
    @Operation(summary = "Gioca il secondo tempo con le decisioni dell'intervallo e poi il resto della giornata",
            description = "Istruzioni nuove (facoltative) e fino a 5 cambi")
    public GiornataGiocataResponse concludi(@AuthenticationPrincipal UtenteAutenticato utente,
            @PathVariable Long carrieraId, @Valid @RequestBody(required = false) DecisioniIntervalloRichiesta decisioni) {
        return avanzamentoService.concludiPartita(carrieraId, utente.id(), decisioni);
    }

    @GetMapping("/partite/{partitaId}")
    @Operation(summary = "Il resoconto di una partita conclusa: statistiche, cronaca e prestazioni")
    public PartitaDettaglioResponse resoconto(@AuthenticationPrincipal UtenteAutenticato utente,
            @PathVariable Long carrieraId, @PathVariable Long partitaId) {
        return partitaService.resoconto(carrieraId, utente.id(), partitaId);
    }
}
