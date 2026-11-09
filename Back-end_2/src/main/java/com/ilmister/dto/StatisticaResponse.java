package com.ilmister.dto;

import java.math.BigDecimal;

public record StatisticaResponse(Long squadraId, String squadra, int possesso, int tiri, int tiriInPorta,
        BigDecimal xg, int falli, int calciDAngolo, int ammonizioni, int espulsioni) {
}
