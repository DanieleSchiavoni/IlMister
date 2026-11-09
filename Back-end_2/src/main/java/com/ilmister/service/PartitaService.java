package com.ilmister.service;

import com.ilmister.dto.PartitaDettaglioResponse;

public interface PartitaService {

    /** Il resoconto di una partita conclusa della carriera */
    PartitaDettaglioResponse resoconto(Long carrieraId, Long utenteId, Long partitaId);
}
