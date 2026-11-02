package com.ilmister.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ilmister.entity.Campionato;
import com.ilmister.entity.Carriera;
import com.ilmister.entity.Giocatore;
import com.ilmister.entity.Squadra;
import com.ilmister.entity.Utente;
import com.ilmister.entity.enumerazioni.Posizione;

class GiocatoreRepositoryTest extends RepositoryTestBase {

    @Autowired
    private GiocatoreRepository giocatoreRepository;

    private Carriera carriera;
    private Carriera altraCarriera;
    private Squadra squadra;
    private Giocatore bianchi;

    @BeforeEach
    void preparaDati() {
        Utente utente = creaUtente("mario");
        carriera = creaCarriera(utente, "Mia");
        altraCarriera = creaCarriera(utente, "Altra");
        Campionato campionato = creaCampionato(carriera, "Campionato", 1);
        squadra = creaSquadra(carriera, campionato, "Squadra A");

        creaGiocatore(carriera, squadra, "Verdi", Posizione.PORTIERE, 60);
        bianchi = creaGiocatore(carriera, squadra, "Bianchi", Posizione.ATTACCANTE, 70);
        creaGiocatore(carriera, squadra, "Neri", Posizione.MEDIANO, 65);
        creaGiocatore(carriera, null, "Svincolato", Posizione.TREQUARTISTA, 55);
        creaGiocatore(altraCarriera, null, "SvincolatoAltrui", Posizione.ATTACCANTE, 55);
    }

    @Test
    void laRosaESempreInOrdineDiCognome() {
        List<Giocatore> rosa = giocatoreRepository.findBySquadraIdOrderByCognomeAscNomeAsc(squadra.getId());

        assertThat(rosa).extracting(Giocatore::getCognome).containsExactly("Bianchi", "Neri", "Verdi");
        assertThat(giocatoreRepository.countBySquadraId(squadra.getId())).isEqualTo(3);
    }

    @Test
    void glISvincolatiSonoQuelliDellaSolaCarriera() {
        List<Giocatore> svincolati = giocatoreRepository.findByCarrieraIdAndSquadraIsNull(carriera.getId());

        assertThat(svincolati).extracting(Giocatore::getCognome).containsExactly("Svincolato");
    }

    @Test
    void unGiocatoreNonSiTrovaIndicandoUnaCarrieraDiversa() {
        assertThat(giocatoreRepository.findByIdAndCarrieraId(bianchi.getId(), carriera.getId())).isPresent();
        assertThat(giocatoreRepository.findByIdAndCarrieraId(bianchi.getId(), altraCarriera.getId())).isEmpty();
    }
}
