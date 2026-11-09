package com.ilmister.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ilmister.entity.Campionato;
import com.ilmister.entity.Carriera;
import com.ilmister.entity.Squadra;
import com.ilmister.entity.Stagione;
import com.ilmister.entity.VoceClassifica;

class ClassificaRepositoryTest extends RepositoryTestBase {

    @Autowired
    private ClassificaRepository classificaRepository;

    private Stagione stagione;
    private Campionato campionato;
    private Carriera carriera;

    @BeforeEach
    void preparaDati() {
        carriera = creaCarriera(creaUtente("mario"), "Mia");
        campionato = creaCampionato(carriera, "Campionato", 1);
        stagione = creaStagione(carriera, 2026);
    }

    private void riga(String squadra, int punti, int golFatti, int golSubiti) {
        VoceClassifica voce = new VoceClassifica();
        voce.setStagione(stagione);
        voce.setCampionato(campionato);
        voce.setSquadra(creaSquadra(carriera, campionato, squadra));
        voce.setPunti(punti);
        voce.setGolFatti(golFatti);
        voce.setGolSubiti(golSubiti);
        em.persistAndFlush(voce);
    }

    @Test
    void laClassificaSiOrdinaPerPuntiDifferenzaRetiEGolFatti() {
        riga("Quarta", 10, 5, 9);   // 10 punti, -4
        riga("Prima", 15, 12, 4);   // 15 punti
        riga("Terza", 12, 10, 6);   // 12 punti, +4, 10 gol fatti
        riga("Seconda", 12, 14, 10); // 12 punti, +4, 14 gol fatti

        List<VoceClassifica> classifica = classificaRepository.classifica(stagione.getId(), campionato.getId());

        assertThat(classifica).extracting(voce -> voce.getSquadra().getNome())
                .containsExactly("Prima", "Seconda", "Terza", "Quarta");
    }

    @Test
    void aParitaAssolutaDecideIlNomeDellaSquadra() {
        riga("Zeta", 9, 5, 5);
        riga("Alfa", 9, 5, 5);

        List<VoceClassifica> classifica = classificaRepository.classifica(stagione.getId(), campionato.getId());

        assertThat(classifica).extracting(voce -> voce.getSquadra().getNome()).containsExactly("Alfa", "Zeta");
    }

    @Test
    void laRigaDiUnaSquadraSiTrovaPerStagione() {
        riga("Alfa", 3, 1, 0);
        Squadra alfa = classificaRepository.classifica(stagione.getId(), campionato.getId()).get(0).getSquadra();

        assertThat(classificaRepository.findByStagioneIdAndSquadraId(stagione.getId(), alfa.getId())).isPresent();
        assertThat(classificaRepository.findByStagioneIdAndSquadraId(stagione.getId() + 999, alfa.getId())).isEmpty();
    }
}
