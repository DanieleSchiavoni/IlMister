package com.ilmister.service;

import com.ilmister.dto.DecisioniIntervalloRichiesta;
import com.ilmister.dto.GiornataGiocataResponse;
import com.ilmister.dto.PrimoTempoResponse;

public interface AvanzamentoService {

    /**
     * Gioca la prossima giornata di colpo: la partita dell'utente con la sua formazione salvata (o automatica),
     * senza fermarsi all'intervallo. Aggiorna classifica, giocatori e data della carriera.
     */
    GiornataGiocataResponse avanza(Long carrieraId, Long utenteId);

    /**
     * Gioca il primo tempo della partita dell'utente e la ferma all'intervallo, dove si possono cambiare
     * istruzioni e giocatori. Se la partita è già all'intervallo, ne ripropone il primo tempo.
     */
    PrimoTempoResponse giocaPrimoTempo(Long carrieraId, Long utenteId);

    /** Gioca il secondo tempo con le decisioni dell'allenatore e poi tutto il resto della giornata */
    GiornataGiocataResponse concludiPartita(Long carrieraId, Long utenteId, DecisioniIntervalloRichiesta decisioni);
}
