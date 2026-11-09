package com.ilmister.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ilmister.entity.Carriera;
import com.ilmister.entity.Utente;

class CarrieraRepositoryTest extends RepositoryTestBase {

    @Autowired
    private CarrieraRepository carrieraRepository;

    private Utente mario;
    private Utente luca;
    private Carriera carrieraDiMario;

    @BeforeEach
    void preparaDati() {
        mario = creaUtente("mario");
        luca = creaUtente("luca");
        carrieraDiMario = creaCarriera(mario, "Carriera di Mario");
        creaCarriera(mario, "Seconda carriera di Mario");
        creaCarriera(luca, "Carriera di Luca");
    }

    @Test
    void lElencoContieneSoloLeCarriereDellUtente() {
        List<Carriera> carriere = carrieraRepository.findByUtenteIdOrderByDataAggiornamentoDesc(mario.getId());

        assertThat(carriere).hasSize(2);
        assertThat(carriere).allMatch(carriera -> carriera.getUtente().getId().equals(mario.getId()));
        assertThat(carrieraRepository.countByUtenteId(luca.getId())).isEqualTo(1);
    }

    @Test
    void unaCarrieraSiTrovaSoloConIlSuoProprietario() {
        assertThat(carrieraRepository.findByIdAndUtenteId(carrieraDiMario.getId(), mario.getId())).isPresent();
        assertThat(carrieraRepository.findByIdAndUtenteId(carrieraDiMario.getId(), luca.getId())).isEmpty();
    }
}
