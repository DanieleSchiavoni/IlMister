package com.ilmister.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.ilmister.generazione.GeneratoreCampionato;
import com.ilmister.motore.MotorePartita;
import com.ilmister.motore.MotorePartita;
import com.ilmister.service.impl.AnteprimaServiceImpl;

// La sicurezza ha i suoi test (ApiIntegrazioneTest): qui si prova solo il controller, senza filtri
@WebMvcTest(SviluppoController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ AnteprimaServiceImpl.class, MotorePartita.class ,GeneratoreCampionato.class })
@TestPropertySource(properties = "app.sviluppo.abilitato=true")
class SviluppoControllerTest {

    @TestConfiguration
    static class OrologioFisso {
        @Bean
        Clock clock() {
            return Clock.fixed(Instant.parse("2026-10-31T12:00:00Z"), ZoneOffset.UTC);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void anteprimaDelCampionatoDaUnSeme() throws Exception {
        mockMvc.perform(get("/api/sviluppo/anteprima-campionato").param("seme", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seme").value(7))
                .andExpect(jsonPath("$.squadre.length()").value(10))
                .andExpect(jsonPath("$.squadre[0].migliori.length()").value(3));
    }

    @Test
    void rosaCompletaDiUnaSquadra() throws Exception {
        mockMvc.perform(get("/api/sviluppo/anteprima-campionato/squadre/2").param("seme", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.giocatori.length()").value(26));
    }

    @Test
    void partitaDiProvaConCronaca() throws Exception {
        mockMvc.perform(get("/api/sviluppo/partita-di-prova").param("seme", "7").param("casa", "0")
                .param("trasferta", "1").param("semePartita", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.casa").exists())
                .andExpect(jsonPath("$.cronaca[0]").exists());
    }
}
