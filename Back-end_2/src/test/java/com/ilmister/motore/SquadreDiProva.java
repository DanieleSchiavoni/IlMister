package com.ilmister.motore;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.ilmister.entity.enumerazioni.Modulo;
import com.ilmister.generazione.ConfigurazioneGenerazione;
import com.ilmister.generazione.GeneratoreCampionato;
import com.ilmister.generazione.GiocatoreGenerato;
import com.ilmister.generazione.SquadraGenerata;

/** Squadre pronte per i test del motore, ricavate dal generatore di campionati */
final class SquadreDiProva {

    private static final GeneratoreCampionato GENERATORE = new GeneratoreCampionato();

    private SquadreDiProva() {
    }

    static List<SquadraGenerata> campionato(long seme) {
        return GENERATORE.genera(seme, ConfigurazioneGenerazione.predefinita(), LocalDate.of(2026, 7, 1)).squadre();
    }

    /** Tutta la rosa, nel formato del motore; gli identificativi partono da idBase + 1 */
    static List<GiocatoreInCampo> rosa(SquadraGenerata squadra, long idBase) {
        List<GiocatoreInCampo> rosa = new ArrayList<>();
        long id = idBase;
        for (GiocatoreGenerato g : squadra.giocatori()) {
            id++;
            rosa.add(new GiocatoreInCampo(id, g.cognome(), g.posizione(), g.attributi(), 50, 70, 100, g.numeroMaglia()));
        }
        return rosa;
    }

    static SquadraInCampo squadra(SquadraGenerata squadra, Modulo modulo, Istruzioni istruzioni, long idBase) {
        FormazioneAutomatica.Scelta scelta = FormazioneAutomatica.scegli(rosa(squadra, idBase), modulo);
        return new SquadraInCampo(idBase, squadra.nome(), modulo, istruzioni, scelta.titolari(), scelta.panchina());
    }

    static DatiPartita partita(long seme, SquadraGenerata casa, SquadraGenerata trasferta) {
        return new DatiPartita(seme, "Stadio di prova",
                squadra(casa, Modulo.M_4_3_3, Istruzioni.equilibrate(), 1_000),
                squadra(trasferta, Modulo.M_4_3_3, Istruzioni.equilibrate(), 2_000));
    }
}
