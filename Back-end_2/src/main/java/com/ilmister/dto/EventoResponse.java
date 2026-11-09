package com.ilmister.dto;

import com.ilmister.entity.enumerazioni.TipoEvento;

/** Una riga della cronaca; squadraId è nullo per gli eventi che riguardano tutta la partita (inizio, intervallo...) */
public record EventoResponse(int minuto, int minutoRecupero, TipoEvento tipo, Long squadraId, String testo) {
}
