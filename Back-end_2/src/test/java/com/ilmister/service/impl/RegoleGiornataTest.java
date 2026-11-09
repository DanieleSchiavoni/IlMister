package com.ilmister.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RegoleGiornataTest {

    @Test
    void lEnergiaRecuperataNonSuperaCento() {
        assertThat(RegoleGiornata.energiaRecuperata(50)).isEqualTo(90);
        assertThat(RegoleGiornata.energiaRecuperata(80)).isEqualTo(100);
        assertThat(RegoleGiornata.energiaRecuperata(100)).isEqualTo(100);
    }

    @Test
    void laFormaSaleConUnBuonVotoEScendeConUnoScarso() {
        assertThat(RegoleGiornata.nuovaForma(50, 6.0)).isEqualTo(50);
        assertThat(RegoleGiornata.nuovaForma(50, 8.0)).isEqualTo(56);
        assertThat(RegoleGiornata.nuovaForma(50, 4.0)).isEqualTo(44);
        assertThat(RegoleGiornata.nuovaForma(99, 10.0)).isEqualTo(100);
        assertThat(RegoleGiornata.nuovaForma(2, 1.0)).isEqualTo(1);
    }

    @Test
    void ilMoraleCresceConLaVittoriaECalaConLaSconfitta() {
        assertThat(RegoleGiornata.nuovoMorale(70, 2, 0)).isEqualTo(74);
        assertThat(RegoleGiornata.nuovoMorale(70, 1, 1)).isEqualTo(70);
        assertThat(RegoleGiornata.nuovoMorale(70, 0, 3)).isEqualTo(66);
        assertThat(RegoleGiornata.nuovoMorale(98, 1, 0)).isEqualTo(100);
        assertThat(RegoleGiornata.nuovoMorale(2, 0, 1)).isEqualTo(1);
    }

    @Test
    void gliSpettatoriRestanoTraZeroELaCapienzaEDipendonoDalSeme() {
        for (long seme = 1; seme <= 200; seme++) {
            int spettatori = RegoleGiornata.spettatori(5000, 60, seme);
            assertThat(spettatori).isBetween(1, 5000);
        }
        assertThat(RegoleGiornata.spettatori(5000, 60, 9)).isEqualTo(RegoleGiornata.spettatori(5000, 60, 9));
        assertThat(RegoleGiornata.spettatori(5000, 90, 9)).isGreaterThan(RegoleGiornata.spettatori(5000, 10, 9));
    }
}
