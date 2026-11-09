package com.ilmister.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class VoceClassificaTest {

    @Test
    void laVittoriaDaTrePunti() {
        VoceClassifica voce = new VoceClassifica();
        voce.registraPartita(3, 1);

        assertThat(voce.getGiocate()).isEqualTo(1);
        assertThat(voce.getVittorie()).isEqualTo(1);
        assertThat(voce.getPunti()).isEqualTo(3);
        assertThat(voce.getGolFatti()).isEqualTo(3);
        assertThat(voce.getGolSubiti()).isEqualTo(1);
        assertThat(voce.differenzaReti()).isEqualTo(2);
    }

    @Test
    void ilPareggioDaUnPuntoELaSconfittaZero() {
        VoceClassifica voce = new VoceClassifica();
        voce.registraPartita(0, 0);
        voce.registraPartita(0, 2);

        assertThat(voce.getGiocate()).isEqualTo(2);
        assertThat(voce.getPareggi()).isEqualTo(1);
        assertThat(voce.getSconfitte()).isEqualTo(1);
        assertThat(voce.getPunti()).isEqualTo(1);
        assertThat(voce.differenzaReti()).isEqualTo(-2);
    }
}
