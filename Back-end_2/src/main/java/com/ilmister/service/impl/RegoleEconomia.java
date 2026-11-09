package com.ilmister.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

/** Gli incassi e le spese di una giornata: funzioni pure */
final class RegoleEconomia {

    /** Quanto paga ogni spettatore */
    static final BigDecimal PREZZO_BIGLIETTO = new BigDecimal("12");

    private RegoleEconomia() {
    }

    static BigDecimal incassoBiglietti(int spettatori) {
        return PREZZO_BIGLIETTO.multiply(BigDecimal.valueOf(Math.max(0, spettatori))).setScale(2, RoundingMode.HALF_UP);
    }

    /** Gli stipendi annui si pagano a rate uguali, una per giornata di campionato */
    static BigDecimal stipendiGiornata(Collection<BigDecimal> stipendiAnnui, int giornateTotali) {
        if (giornateTotali <= 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        BigDecimal totale = stipendiAnnui.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return totale.divide(BigDecimal.valueOf(giornateTotali), 2, RoundingMode.HALF_UP);
    }
}
