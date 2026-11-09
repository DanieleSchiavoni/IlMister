package com.ilmister.controller;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ilmister.dto.AnteprimaCampionatoResponse;
import com.ilmister.dto.AnteprimaRosaResponse;
import com.ilmister.dto.PartitaDiProvaResponse;
import com.ilmister.service.AnteprimaService;

import lombok.RequiredArgsConstructor;

/**
 * Strumenti per lo sviluppo: si vedono i campionati generati senza account né sicurezza.
 * Esiste solo se app.sviluppo.abilitato=true (profilo dev); in produzione non c'è.
 */
@RestController
@RequestMapping("/api/sviluppo")
@ConditionalOnProperty(name = "app.sviluppo.abilitato", havingValue = "true")
@RequiredArgsConstructor
public class SviluppoController {

    private final AnteprimaService anteprimaService;

    @GetMapping("/anteprima-campionato")
    public AnteprimaCampionatoResponse anteprima(@RequestParam(defaultValue = "1") long seme) {
        return anteprimaService.anteprima(seme);
    }

    @GetMapping("/anteprima-campionato/squadre/{indice}")
    public AnteprimaRosaResponse rosa(@PathVariable int indice, @RequestParam(defaultValue = "1") long seme) {
        return anteprimaService.rosa(seme, indice);
    }

    /** Gioca per prova una partita tra due squadre dell'anteprima e ne mostra la cronaca */
    @GetMapping("/partita-di-prova")
    public PartitaDiProvaResponse partitaDiProva(@RequestParam(defaultValue = "1") long seme,
            @RequestParam(defaultValue = "0") int casa, @RequestParam(defaultValue = "1") int trasferta,
            @RequestParam(defaultValue = "1") long semePartita) {
        return anteprimaService.partitaDiProva(seme, casa, trasferta, semePartita);
    }
}
