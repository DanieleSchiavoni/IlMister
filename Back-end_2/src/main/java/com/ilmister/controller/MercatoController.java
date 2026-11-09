package com.ilmister.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ilmister.dto.EsitoTrattativaResponse;
import com.ilmister.dto.GiocatoreIdRichiesta;
import com.ilmister.dto.GiocatoreMercatoResponse;
import com.ilmister.dto.OffertaRichiesta;
import com.ilmister.entity.enumerazioni.Ruolo;
import com.ilmister.security.UtenteAutenticato;
import com.ilmister.service.MercatoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/carriere/{carrieraId}/mercato")
@RequiredArgsConstructor
@Tag(name = "Mercato", description = "Acquisti, cessioni, svincoli e ingaggi")
public class MercatoController {

    private final MercatoService mercatoService;

    @GetMapping("/giocatori")
    @Operation(summary = "Cerca giocatori delle altre squadre",
            description = "I più forti per primi, al massimo 60. Filtri facoltativi: ruolo, nome, valore massimo.")
    public List<GiocatoreMercatoResponse> cerca(@AuthenticationPrincipal UtenteAutenticato utente,
            @PathVariable Long carrieraId, @RequestParam(required = false) Ruolo ruolo,
            @RequestParam(required = false) String nome, @RequestParam(required = false) BigDecimal valoreMassimo) {
        return mercatoService.cerca(carrieraId, utente.id(), ruolo, nome, valoreMassimo);
    }

    @GetMapping("/svincolati")
    @Operation(summary = "I giocatori senza squadra")
    public List<GiocatoreMercatoResponse> svincolati(@AuthenticationPrincipal UtenteAutenticato utente,
            @PathVariable Long carrieraId) {
        return mercatoService.svincolati(carrieraId, utente.id());
    }

    @PostMapping("/offerte")
    @Operation(summary = "Fa un'offerta per un giocatore di un'altra squadra",
            description = "Se la squadra rifiuta si risponde 200 con conclusa = false; gli errori veri sono 400/404.")
    public EsitoTrattativaResponse offri(@AuthenticationPrincipal UtenteAutenticato utente,
            @PathVariable Long carrieraId, @Valid @RequestBody OffertaRichiesta richiesta) {
        return mercatoService.offri(carrieraId, utente.id(), richiesta.giocatoreId(), richiesta.importo());
    }

    @PostMapping("/vendite")
    @Operation(summary = "Vende un giocatore a una squadra che può permetterselo (85% del valore)")
    public EsitoTrattativaResponse vendi(@AuthenticationPrincipal UtenteAutenticato utente,
            @PathVariable Long carrieraId, @Valid @RequestBody GiocatoreIdRichiesta richiesta) {
        return mercatoService.vendi(carrieraId, utente.id(), richiesta.giocatoreId());
    }

    @PostMapping("/svincoli")
    @Operation(summary = "Rescinde il contratto di un giocatore: diventa svincolato")
    public EsitoTrattativaResponse svincola(@AuthenticationPrincipal UtenteAutenticato utente,
            @PathVariable Long carrieraId, @Valid @RequestBody GiocatoreIdRichiesta richiesta) {
        return mercatoService.svincola(carrieraId, utente.id(), richiesta.giocatoreId());
    }

    @PostMapping("/ingaggi")
    @Operation(summary = "Ingaggia un giocatore svincolato")
    public EsitoTrattativaResponse ingaggia(@AuthenticationPrincipal UtenteAutenticato utente,
            @PathVariable Long carrieraId, @Valid @RequestBody GiocatoreIdRichiesta richiesta) {
        return mercatoService.ingaggia(carrieraId, utente.id(), richiesta.giocatoreId());
    }
}
