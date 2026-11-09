package com.ilmister.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ilmister.dto.AnteprimaCampionatoResponse;
import com.ilmister.dto.AnteprimaRosaResponse;
import com.ilmister.service.AnteprimaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/anteprima")
@RequiredArgsConstructor
@Tag(name = "Nuova carriera", description = "Si guarda il campionato che nasce da un seme prima di sceglierlo")
public class AnteprimaController {

    private final AnteprimaService anteprimaService;

    @GetMapping("/campionato")
    @Operation(summary = "Le squadre del campionato generato da un seme")
    public AnteprimaCampionatoResponse campionato(@RequestParam(defaultValue = "1") long seme) {
        return anteprimaService.anteprima(seme);
    }

    @GetMapping("/campionato/squadre/{indice}")
    @Operation(summary = "La rosa di una squadra dell'anteprima")
    public AnteprimaRosaResponse rosa(@PathVariable int indice, @RequestParam(defaultValue = "1") long seme) {
        return anteprimaService.rosa(seme, indice);
    }
}
