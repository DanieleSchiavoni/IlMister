package com.ilmister.service;

import java.math.BigDecimal;
import java.util.List;

import com.ilmister.dto.EsitoTrattativaResponse;
import com.ilmister.dto.GiocatoreMercatoResponse;
import com.ilmister.entity.enumerazioni.Ruolo;

public interface MercatoService {

    /** I giocatori delle altre squadre, i più forti per primi (al massimo 60), con filtri facoltativi */
    List<GiocatoreMercatoResponse> cerca(Long carrieraId, Long utenteId, Ruolo ruolo, String nome,
            BigDecimal valoreMassimo);

    /** I giocatori senza squadra, ingaggiabili senza costo di cartellino */
    List<GiocatoreMercatoResponse> svincolati(Long carrieraId, Long utenteId);

    /** Un'offerta per un giocatore di un'altra squadra: la squadra del computer accetta o rifiuta */
    EsitoTrattativaResponse offri(Long carrieraId, Long utenteId, Long giocatoreId, BigDecimal importo);

    /** Vende un giocatore dell'utente a una squadra del computer che può permetterselo */
    EsitoTrattativaResponse vendi(Long carrieraId, Long utenteId, Long giocatoreId);

    /** Rescinde il contratto di un giocatore dell'utente: diventa svincolato, senza incassi */
    EsitoTrattativaResponse svincola(Long carrieraId, Long utenteId, Long giocatoreId);

    /** Ingaggia un giocatore svincolato */
    EsitoTrattativaResponse ingaggia(Long carrieraId, Long utenteId, Long giocatoreId);
}
