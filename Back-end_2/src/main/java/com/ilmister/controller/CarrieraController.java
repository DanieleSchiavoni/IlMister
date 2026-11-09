package com.ilmister.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ilmister.dto.CarrieraResponse;
import com.ilmister.dto.GiornataResponse;
import com.ilmister.dto.NuovaCarrieraRichiesta;
import com.ilmister.dto.RigaClassificaResponse;
import com.ilmister.entity.Carriera;
import com.ilmister.security.UtenteAutenticato;
import com.ilmister.service.CalendarioService;
import com.ilmister.service.CarrieraService;
import com.ilmister.service.ClassificaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Le carriere dell'utente: ognuno vede e modifica solo le proprie (le altre rispondono 404) */
@RestController
@RequestMapping("/api/carriere")
@RequiredArgsConstructor
@Tag(name = "Carriere")
public class CarrieraController {

    private final CarrieraService carrieraService;
    private final CalendarioService calendarioService;
    private final ClassificaService classificaService;

    @GetMapping
    @Operation(summary = "Le carriere dell'utente, le più recenti per prime")
    public List<CarrieraResponse> carriere(@AuthenticationPrincipal UtenteAutenticato utente) {
        return carrieraService.carriere(utente.id());
    }

    @PostMapping
    @Operation(summary = "Inizia una nuova carriera",
            description = "Il campionato nasce dal seme e la squadra si sceglie per nome tra quelle dell'anteprima. "
                    + "Massimo 5 carriere per utente.")
    public ResponseEntity<CarrieraResponse> crea(@AuthenticationPrincipal UtenteAutenticato utente,
            @Valid @RequestBody NuovaCarrieraRichiesta richiesta) {
        Carriera creata = carrieraService.creaCarriera(utente.id(), richiesta);
        return ResponseEntity.created(URI.create("/api/carriere/" + creata.getId()))
                .body(carrieraService.carriera(creata.getId(), utente.id()));
    }

    @GetMapping("/{carrieraId}")
    @Operation(summary = "Una carriera")
    public CarrieraResponse carriera(@AuthenticationPrincipal UtenteAutenticato utente, @PathVariable Long carrieraId) {
        return carrieraService.carriera(carrieraId, utente.id());
    }

    @DeleteMapping("/{carrieraId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina la carriera con tutti i suoi dati")
    public void elimina(@AuthenticationPrincipal UtenteAutenticato utente, @PathVariable Long carrieraId) {
        carrieraService.elimina(carrieraId, utente.id());
    }

    @GetMapping("/{carrieraId}/calendario")
    @Operation(summary = "Il calendario della stagione, con i risultati già giocati")
    public List<GiornataResponse> calendario(@AuthenticationPrincipal UtenteAutenticato utente,
            @PathVariable Long carrieraId) {
        return calendarioService.calendario(carrieraId, utente.id());
    }

    @GetMapping("/{carrieraId}/classifica")
    @Operation(summary = "La classifica del campionato")
    public List<RigaClassificaResponse> classifica(@AuthenticationPrincipal UtenteAutenticato utente,
            @PathVariable Long carrieraId) {
        return classificaService.classifica(carrieraId, utente.id());
    }
}
