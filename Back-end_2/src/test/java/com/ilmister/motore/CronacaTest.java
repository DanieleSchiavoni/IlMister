package com.ilmister.motore;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.SplittableRandom;

import org.junit.jupiter.api.Test;

class CronacaTest {

    private void controlla(String testo) {
        assertThat(testo.isBlank()).isFalse();
        assertThat(testo.contains("%")).as(testo).isFalse();
        assertThat(testo.contains("null")).as(testo).isFalse();
    }

    /** Ogni frase viene generata molte volte, con semi diversi, così si provano tutte le varianti */
    @Test
    void tutteLeFrasiSiComponganoSenzaErroriENonLasciaSegnaposto() {
        for (long seme = 1; seme <= 200; seme++) {
            SplittableRandom r = new SplittableRandom(seme);
            controlla(Cronaca.inizio("Virtus Valmora", "Real Borgoalto", "Stadio Comunale Valmora"));
            controlla(Cronaca.intervallo("Virtus Valmora", 1, 0, "Real Borgoalto"));
            controlla(Cronaca.inizioSecondoTempo());
            controlla(Cronaca.fine("Virtus Valmora", 2, 2, "Real Borgoalto"));
            controlla(Cronaca.gol(r, "Rossi", "Bianchi", "Virtus Valmora", false));
            controlla(Cronaca.gol(r, "Rossi", null, "Virtus Valmora", false));
            controlla(Cronaca.gol(r, "Rossi", null, "Virtus Valmora", true));
            controlla(Cronaca.tiroParato(r, "Rossi", "Verdi"));
            controlla(Cronaca.tiroFuori(r, "Rossi"));
            controlla(Cronaca.tiroBloccato(r, "Rossi"));
            controlla(Cronaca.palo(r, "Rossi"));
            controlla(Cronaca.occasione(r, "Rossi", "Virtus Valmora"));
            controlla(Cronaca.angolo(r, "Virtus Valmora"));
            controlla(Cronaca.rigoreAssegnato(r, "Virtus Valmora"));
            controlla(Cronaca.fallo(r, "Rossi"));
            controlla(Cronaca.ammonizione(r, "Rossi", "Virtus Valmora"));
            controlla(Cronaca.espulsione(r, "Rossi", "Virtus Valmora", true));
            controlla(Cronaca.espulsione(r, "Rossi", "Virtus Valmora", false));
            controlla(Cronaca.infortunio(r, "Rossi", "Virtus Valmora"));
            controlla(Cronaca.sostituzione(r, "Virtus Valmora", "Rossi", "Bianchi", true));
            controlla(Cronaca.sostituzione(r, "Virtus Valmora", "Rossi", "Bianchi", false));
        }
    }

    @Test
    void laCronacaCitaIProtagonisti() {
        SplittableRandom r = new SplittableRandom(1);

        String gol = Cronaca.gol(r, "Rossi", "Bianchi", "Virtus Valmora", false);

        assertThat(gol.contains("Rossi")).isTrue();
        assertThat(gol.contains("Bianchi")).isTrue();
    }
}
