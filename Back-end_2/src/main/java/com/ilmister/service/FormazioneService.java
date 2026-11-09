package com.ilmister.service;

import com.ilmister.dto.FormazioneRichiesta;
import com.ilmister.dto.FormazioneResponse;

public interface FormazioneService {

    /** La formazione salvata della squadra dell'utente o, se manca, una proposta automatica */
    FormazioneResponse formazione(Long carrieraId, Long utenteId);

    /** Salva la formazione dopo aver controllato che sia valida */
    FormazioneResponse salva(Long carrieraId, Long utenteId, FormazioneRichiesta richiesta);
}
