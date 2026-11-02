package com.ilmister.service;

import com.ilmister.dto.AnteprimaCampionatoResponse;
import com.ilmister.dto.AnteprimaRosaResponse;

/** Mostra il campionato che nascerebbe da un seme, senza salvare niente: serve a scegliere la squadra */
public interface AnteprimaService {

    AnteprimaCampionatoResponse anteprima(long seme);

    /** La rosa completa di una squadra dell'anteprima, scelta con il suo indice (da 0) */
    AnteprimaRosaResponse rosa(long seme, int indiceSquadra);
}
