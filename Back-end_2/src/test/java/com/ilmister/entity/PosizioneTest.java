package com.ilmister.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.ilmister.entity.enumerazioni.Posizione;
import com.ilmister.entity.enumerazioni.Ruolo;

class PosizioneTest {

    @Test
    void ilRepartoSiRicavaDallaPosizione() {
        assertThat(Posizione.PORTIERE.getRuolo()).isEqualTo(Ruolo.PORTIERE);
        assertThat(Posizione.TERZINO_DESTRO.getRuolo()).isEqualTo(Ruolo.DIFENSORE);
        assertThat(Posizione.DIFENSORE_CENTRALE.getRuolo()).isEqualTo(Ruolo.DIFENSORE);
        assertThat(Posizione.MEDIANO.getRuolo()).isEqualTo(Ruolo.CENTROCAMPISTA);
        assertThat(Posizione.TREQUARTISTA.getRuolo()).isEqualTo(Ruolo.CENTROCAMPISTA);
        assertThat(Posizione.ESTERNO_SINISTRO.getRuolo()).isEqualTo(Ruolo.CENTROCAMPISTA);
        assertThat(Posizione.ALA_DESTRA.getRuolo()).isEqualTo(Ruolo.ATTACCANTE);
        assertThat(Posizione.ATTACCANTE.getRuolo()).isEqualTo(Ruolo.ATTACCANTE);
    }

    @Test
    void ogniRepartoHaAlmenoUnaPosizione() {
        for (Ruolo ruolo : Ruolo.values()) {
            boolean presente = Arrays.stream(Posizione.values()).anyMatch(posizione -> posizione.getRuolo() == ruolo);
            assertThat(presente).as(ruolo.name()).isTrue();
        }
    }

    @Test
    void leSigleSonoTutteDiverse() {
        Set<String> sigle = Arrays.stream(Posizione.values()).map(Posizione::getSigla).collect(Collectors.toSet());

        assertThat(sigle).hasSize(Posizione.values().length);
    }
}
