package com.ilmister.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.ilmister.entity.enumerazioni.Modulo;
import com.ilmister.generazione.ConfigurazioneGenerazione;
import com.ilmister.generazione.GeneratoreCampionato;
import com.ilmister.generazione.GiocatoreGenerato;
import com.ilmister.generazione.SquadraGenerata;
import com.ilmister.motore.DatiPartita;
import com.ilmister.motore.FormazioneAutomatica;
import com.ilmister.motore.GiocatoreInCampo;
import com.ilmister.motore.Istruzioni;
import com.ilmister.motore.MotorePartita;
import com.ilmister.motore.SquadraInCampo;

class StatoPartitaJsonTest {

    private static SquadraInCampo squadra(SquadraGenerata generata, Modulo modulo, long idBase) {
        long id = idBase;
        List<GiocatoreInCampo> rosa = new java.util.ArrayList<>();
        for (GiocatoreGenerato g : generata.giocatori()) {
            rosa.add(new GiocatoreInCampo(++id, g.cognome(), g.posizione(), g.attributi(), 55, 65, 90, g.numeroMaglia()));
        }
        FormazioneAutomatica.Scelta scelta = FormazioneAutomatica.scegli(rosa, modulo);
        return new SquadraInCampo(idBase, generata.nome(), modulo, Istruzioni.equilibrate(), scelta.titolari(),
                scelta.panchina());
    }

    private static DatiPartita dati() {
        List<SquadraGenerata> squadre = new GeneratoreCampionato()
                .genera(5, ConfigurazioneGenerazione.predefinita(), LocalDate.of(2026, 7, 1)).squadre();
        return new DatiPartita(123L, "Stadio di prova", squadra(squadre.get(0), Modulo.M_4_3_3, 1_000),
                squadra(squadre.get(1), Modulo.M_3_5_2, 2_000));
    }

    @Test
    void ilGiroDiScritturaELetturaDaLoStessoOggetto() {
        DatiPartita originali = dati();

        DatiPartita riletti = StatoPartitaJson.leggi(StatoPartitaJson.scrivi(originali));

        assertThat(riletti).isEqualTo(originali);
    }

    @Test
    void conIDatiRilettiLaPartitaEIdentica() {
        DatiPartita originali = dati();
        MotorePartita motore = new MotorePartita();

        var prima = motore.giocaPartita(originali);
        var dopo = motore.giocaPartita(StatoPartitaJson.leggi(StatoPartitaJson.scrivi(originali)));

        assertThat(dopo).isEqualTo(prima);
    }
}
