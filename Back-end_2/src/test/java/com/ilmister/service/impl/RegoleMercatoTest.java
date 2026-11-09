package com.ilmister.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.ilmister.service.impl.RegoleMercato.Giudizio;

class RegoleMercatoTest {

    private static final BigDecimal VALORE = new BigDecimal("30000.00");
    private static final LocalDate GIORNO = LocalDate.of(2026, 7, 1);

    @Test
    void laSogliaVaDalCentoAlCentoVenticinquePerCentoDelValore() {
        for (long giocatore = 1; giocatore <= 500; giocatore++) {
            BigDecimal soglia = RegoleMercato.soglia(VALORE, false, 7, giocatore, GIORNO);
            assertThat(soglia).isBetween(new BigDecimal("30000"), new BigDecimal("37500"));
        }
    }

    @Test
    void iTitolariCostanoPiuDeiRincalzi() {
        for (long giocatore = 1; giocatore <= 100; giocatore++) {
            BigDecimal rincalzo = RegoleMercato.soglia(VALORE, false, 7, giocatore, GIORNO);
            BigDecimal titolare = RegoleMercato.soglia(VALORE, true, 7, giocatore, GIORNO);
            assertThat(titolare).isGreaterThan(rincalzo);
            assertThat(titolare).isLessThanOrEqualTo(new BigDecimal("41300"));
        }
    }

    @Test
    void lastessaOffertaNelloStessoGiornoHaSempreLoStessoEsito() {
        assertThat(RegoleMercato.soglia(VALORE, false, 7, 42, GIORNO))
                .isEqualTo(RegoleMercato.soglia(VALORE, false, 7, 42, GIORNO));
    }

    @Test
    void conIlPassareDeiGiorniLaSogliaCambia() {
        long diverse = java.util.stream.LongStream.rangeClosed(1, 30)
                .mapToObj(giorno -> RegoleMercato.soglia(VALORE, false, 7, 42, GIORNO.plusDays(giorno)))
                .distinct().count();
        assertThat(diverse).isGreaterThan(10);
    }

    @Test
    void lOffertaSiGiudicaInTreFasce() {
        BigDecimal soglia = new BigDecimal("40000.00");

        assertThat(RegoleMercato.giudica(soglia, soglia)).isEqualTo(Giudizio.ACCETTATA);
        assertThat(RegoleMercato.giudica(new BigDecimal("50000"), soglia)).isEqualTo(Giudizio.ACCETTATA);
        assertThat(RegoleMercato.giudica(new BigDecimal("36000"), soglia)).isEqualTo(Giudizio.VICINA);
        assertThat(RegoleMercato.giudica(new BigDecimal("35999"), soglia)).isEqualTo(Giudizio.LONTANA);
    }

    @Test
    void ilPrezzoDiVenditaEIlOttantacinquePerCentoArrotondatoAlleCentinaia() {
        assertThat(RegoleMercato.prezzoVendita(new BigDecimal("30000.00"))).isEqualByComparingTo("25500");
        assertThat(RegoleMercato.prezzoVendita(new BigDecimal("12345.00"))).isEqualByComparingTo("10500");
    }
}
