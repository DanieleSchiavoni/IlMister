package com.ilmister.service.impl;

import java.time.LocalDate;

import com.ilmister.dto.GiocatoreResponse;
import com.ilmister.entity.Giocatore;
import com.ilmister.generazione.Attributi;
import com.ilmister.motore.GiocatoreInCampo;

/** Le conversioni di un giocatore: verso la risposta dell'API e verso il formato del motore di partita */
final class GiocatoreMapper {

    private GiocatoreMapper() {
    }

    static Attributi attributi(Giocatore giocatore) {
        return new Attributi(giocatore.getVelocita(), giocatore.getResistenza(), giocatore.getForza(),
                giocatore.getTecnica(), giocatore.getPassaggio(), giocatore.getTiro(), giocatore.getDribbling(),
                giocatore.getContrasto(), giocatore.getIntelligenza(), giocatore.getParate());
    }

    /** Il giocatore come lo vede il motore; l'energia è quella con cui entrerà in campo */
    static GiocatoreInCampo inCampo(Giocatore giocatore, int energia) {
        int maglia = giocatore.getNumeroMaglia() == null ? 0 : giocatore.getNumeroMaglia();
        return new GiocatoreInCampo(giocatore.getId(), giocatore.getCognome(), giocatore.getPosizione(),
                attributi(giocatore), giocatore.getForma(), giocatore.getMorale(), energia, maglia);
    }

    /** Il giocatore per l'API; infortuni e squalifiche si valutano alla data indicata */
    static GiocatoreResponse aResponse(Giocatore giocatore, LocalDate data) {
        boolean infortunato = giocatore.infortunatoAl(data);
        return new GiocatoreResponse(giocatore.getId(), giocatore.getNome(), giocatore.getCognome(),
                giocatore.getPosizione().getDescrizione(), giocatore.getPosizione().getSigla(),
                giocatore.getRuolo().name(), giocatore.getNumeroMaglia(), giocatore.etaAl(data),
                giocatore.getNazionalita(), giocatore.getPiede().name(), giocatore.getValutazione(),
                giocatore.getPotenziale(), attributi(giocatore), giocatore.getForma(), giocatore.getMorale(),
                giocatore.getEnergia(), infortunato, infortunato ? giocatore.getTipoInfortunio() : null,
                infortunato ? giocatore.getInfortunatoFinoAl() : null, giocatore.getSqualificaGiornate(),
                giocatore.disponibileAl(data));
    }
}
