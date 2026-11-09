package com.ilmister.service;

import com.ilmister.dto.AnteprimaCampionatoResponse;
import com.ilmister.dto.AnteprimaRosaResponse;
import com.ilmister.dto.PartitaDiProvaResponse;

/** Mostra il campionato che nascerebbe da un seme, senza salvare niente: serve a scegliere la squadra */
public interface AnteprimaService {

    AnteprimaCampionatoResponse anteprima(long seme);

    /** La rosa completa di una squadra dell'anteprima, scelta con il suo indice (da 0) */
    AnteprimaRosaResponse rosa(long seme, int indiceSquadra);

    /**
     * Gioca per prova una partita tra due squadre dell'anteprima (scelte con l'indice, da 0), entrambe in 4-3-3
     * con istruzioni equilibrate. Il seme della partita decide l'esito: lo stesso seme dà la stessa partita.
     */
    PartitaDiProvaResponse partitaDiProva(long seme, int indiceCasa, int indiceTrasferta, long semePartita);
}
