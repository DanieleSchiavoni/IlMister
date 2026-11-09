package com.ilmister.dto;

import java.math.BigDecimal;

public record PrestazioneResponse(Long giocatoreId, String giocatore, String siglaPosizione, Long squadraId,
        boolean titolare, int minutiGiocati, int gol, int assist, int tiri, boolean ammonito, boolean espulso,
        BigDecimal voto) {
}
