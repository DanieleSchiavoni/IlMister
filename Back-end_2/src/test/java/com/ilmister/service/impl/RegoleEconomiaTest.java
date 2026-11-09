package com.ilmister.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

class RegoleEconomiaTest {

    @Test
    void ogniSpettatorePagaIlBiglietto() {
        assertThat(RegoleEconomia.incassoBiglietti(2800)).isEqualByComparingTo("33600");
        assertThat(RegoleEconomia.incassoBiglietti(0)).isEqualByComparingTo("0");
        assertThat(RegoleEconomia.incassoBiglietti(-5)).isEqualByComparingTo("0");
    }

    @Test
    void gliStipendiAnnuiSiPaganoARateUgualiUnaPerGiornata() {
        List<BigDecimal> stipendi = List.of(new BigDecimal("5400.00"), new BigDecimal("3600.00"));

        assertThat(RegoleEconomia.stipendiGiornata(stipendi, 18)).isEqualByComparingTo("500.00");
    }

    @Test
    void laRataSiArrotondaAiCentesimi() {
        assertThat(RegoleEconomia.stipendiGiornata(List.of(new BigDecimal("1000.00")), 18))
                .isEqualByComparingTo("55.56");
    }

    @Test
    void senzaGiornateOSenzaGiocatoriNonSiPagaNiente() {
        assertThat(RegoleEconomia.stipendiGiornata(List.of(new BigDecimal("1000")), 0)).isEqualByComparingTo("0");
        assertThat(RegoleEconomia.stipendiGiornata(List.of(), 18)).isEqualByComparingTo("0");
    }
}
