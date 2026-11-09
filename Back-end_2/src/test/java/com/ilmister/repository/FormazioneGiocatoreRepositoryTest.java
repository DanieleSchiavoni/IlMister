package com.ilmister.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ilmister.entity.Formazione;
import com.ilmister.entity.FormazioneGiocatore;
import com.ilmister.entity.Giocatore;
import com.ilmister.entity.Squadra;
import com.ilmister.entity.enumerazioni.Modulo;
import com.ilmister.entity.enumerazioni.Posizione;

class FormazioneGiocatoreRepositoryTest extends RepositoryTestBase {

    @Autowired
    private FormazioneRepository formazioneRepository;
    @Autowired
    private FormazioneGiocatoreRepository formazioneGiocatoreRepository;

    private Squadra squadra;
    private Formazione formazione;
    private Giocatore portiere;
    private Giocatore difensore;
    private Giocatore attaccante;

    @BeforeEach
    void preparaDati() {
        var carriera = creaCarriera(creaUtente("mario"), "Mia");
        squadra = creaSquadra(carriera, creaCampionato(carriera, "Campionato", 1), "Squadra A");
        portiere = creaGiocatore(carriera, squadra, "Portieri", Posizione.PORTIERE, 60);
        difensore = creaGiocatore(carriera, squadra, "Difensori", Posizione.DIFENSORE_CENTRALE, 62);
        attaccante = creaGiocatore(carriera, squadra, "Attaccanti", Posizione.ATTACCANTE, 70);

        formazione = new Formazione();
        formazione.setSquadra(squadra);
        formazione.setModulo(Modulo.M_4_3_3);
        em.persistAndFlush(formazione);
    }

    private void schiera(Giocatore giocatore, int posto) {
        FormazioneGiocatore schierato = new FormazioneGiocatore();
        schierato.setFormazione(formazione);
        schierato.setGiocatore(giocatore);
        schierato.setPosto(posto);
        em.persist(schierato);
    }

    @Test
    void laFormazioneDiUnaSquadraSiTrovaPerSquadra() {
        assertThat(formazioneRepository.findBySquadraId(squadra.getId())).isPresent();
        assertThat(formazioneRepository.findBySquadraId(squadra.getId() + 999)).isEmpty();
    }

    @Test
    void riscrivereLaFormazioneNonViolaIVincoliSuiPosti() {
        schiera(portiere, 0);
        schiera(difensore, 1);
        schiera(attaccante, 11);
        em.flush();

        // Si riusano gli stessi posti con giocatori diversi: funziona solo se prima si cancella con la query
        int eliminati = formazioneGiocatoreRepository.eliminaPerFormazione(formazione.getId());
        schiera(attaccante, 0);
        schiera(portiere, 1);
        em.flush();
        em.clear();

        List<FormazioneGiocatore> schierati = formazioneGiocatoreRepository
                .findByFormazioneIdOrderByPostoAsc(formazione.getId());

        assertThat(eliminati).isEqualTo(3);
        assertThat(schierati).extracting(FormazioneGiocatore::getPosto).containsExactly(0, 1);
        assertThat(schierati).extracting(schierato -> schierato.getGiocatore().getCognome())
                .containsExactly("Attaccanti", "Portieri");
        assertThat(schierati).allMatch(FormazioneGiocatore::titolare);
    }

    @Test
    void daPostoUndiciInPoiSiEInPanchina() {
        schiera(portiere, 10);
        schiera(difensore, 11);
        em.flush();

        List<FormazioneGiocatore> schierati = formazioneGiocatoreRepository
                .findByFormazioneIdOrderByPostoAsc(formazione.getId());

        assertThat(schierati.get(0).titolare()).isTrue();
        assertThat(schierati.get(1).titolare()).isFalse();
    }
}
