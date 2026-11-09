package com.ilmister.motore;

import java.math.BigDecimal;

import com.ilmister.entity.enumerazioni.TipoEvento;

/**
 * Una riga della cronaca. Il primo giocatore è il protagonista (chi tira, segna, è ammonito, esce);
 * il secondo è chi lo aiuta (assist) o chi entra al suo posto.
 *
 * @param ordine il numero progressivo nella cronaca, da 1
 * @param lato   la squadra coinvolta, nulla per gli eventi che riguardano la partita
 * @param xg     i gol attesi, solo per le conclusioni a rete
 */
public record EventoSimulato(int ordine, int minuto, int minutoRecupero, TipoEvento tipo, Lato lato, Long giocatoreId,
        Long secondoGiocatoreId, BigDecimal xg, String testo) {
}
