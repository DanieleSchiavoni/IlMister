package com.ilmister.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import com.ilmister.dto.NuovaCarrieraRichiesta;
import com.ilmister.entity.Carriera;
import com.ilmister.entity.Giocatore;
import com.ilmister.entity.Squadra;
import com.ilmister.entity.Utente;
import com.ilmister.entity.enumerazioni.StatoStagione;
import com.ilmister.exception.OperazioneNonValidaException;
import com.ilmister.exception.RisorsaNonTrovataException;
import com.ilmister.generazione.CalcolatoreAttributi;
import com.ilmister.generazione.Attributi;
import com.ilmister.generazione.ConfigurazioneGenerazione;
import com.ilmister.generazione.GeneratoreCampionato;
import com.ilmister.repository.CampionatoRepository;
import com.ilmister.repository.CarrieraRepository;
import com.ilmister.repository.GiocatoreRepository;
import com.ilmister.repository.RepositoryTestBase;
import com.ilmister.repository.SquadraRepository;
import com.ilmister.repository.StagioneRepository;
import com.ilmister.service.impl.CarrieraServiceImpl;

@Import({ CarrieraServiceImpl.class, GeneratoreCampionato.class })
class CarrieraServiceImplTest extends RepositoryTestBase {

    @TestConfiguration
    static class OrologioFisso {
        @Bean
        Clock clock() {
            return Clock.fixed(Instant.parse("2026-10-31T12:00:00Z"), ZoneOffset.UTC);
        }
    }

    @Autowired
    private CarrieraService carrieraService;
    @Autowired
    private GeneratoreCampionato generatore;
    @Autowired
    private CarrieraRepository carrieraRepository;
    @Autowired
    private StagioneRepository stagioneRepository;
    @Autowired
    private CampionatoRepository campionatoRepository;
    @Autowired
    private SquadraRepository squadraRepository;
    @Autowired
    private GiocatoreRepository giocatoreRepository;

    private Utente mario;

    @BeforeEach
    void preparaDati() {
        mario = creaUtente("mario");
    }

    private String nomeSquadra(long seme, int indice) {
        return generatore.genera(seme, ConfigurazioneGenerazione.predefinita(), LocalDate.of(2026, 7, 1))
                .squadre().get(indice).nome();
    }

    private NuovaCarrieraRichiesta richiesta(long seme, int indiceSquadra) {
        return new NuovaCarrieraRichiesta("La mia carriera", "Mister Prova", nomeSquadra(seme, indiceSquadra), seme);
    }

    @Test
    void creaLaCarrieraConCampionatoSquadreEGiocatori() {
        Carriera carriera = carrieraService.creaCarriera(mario.getId(), richiesta(7, 3));
        em.flush();
        em.clear();

        assertThat(carrieraRepository.count()).isEqualTo(1);
        assertThat(campionatoRepository.findByCarrieraIdOrderByLivelloAscNomeAsc(carriera.getId())).hasSize(1);
        assertThat(squadraRepository.findByCarrieraId(carriera.getId())).hasSize(10);
        assertThat(giocatoreRepository.count()).isEqualTo(10 * 26);

        Carriera salvata = carrieraRepository.findById(carriera.getId()).orElseThrow();
        assertThat(salvata.getNome()).isEqualTo("La mia carriera");
        assertThat(salvata.getNomeAllenatore()).isEqualTo("Mister Prova");
        assertThat(salvata.getSeme()).isEqualTo(7);
        assertThat(salvata.getDataCorrente()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(salvata.getFiduciaPresidente()).isEqualTo(60);
    }

    @Test
    void creaLaPrimaStagioneInCorso() {
        Carriera carriera = carrieraService.creaCarriera(mario.getId(), richiesta(7, 0));

        var stagione = stagioneRepository.findByCarrieraIdAndStato(carriera.getId(), StatoStagione.IN_CORSO).orElseThrow();

        assertThat(stagione.getAnnoInizio()).isEqualTo(2026);
        assertThat(stagione.getDataInizio()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(stagione.getDataFine()).isEqualTo(LocalDate.of(2027, 6, 30));
    }

    @Test
    void alUtenteSiAssegnaSoloLaSquadraScelta() {
        Carriera carriera = carrieraService.creaCarriera(mario.getId(), richiesta(7, 5));

        Squadra allenata = squadraRepository.findByCarrieraIdAndAllenataDallUtenteTrue(carriera.getId()).orElseThrow();

        assertThat(allenata.getNome()).isEqualTo(nomeSquadra(7, 5));
        assertThat(squadraRepository.findByCarrieraId(carriera.getId()).stream()
                .filter(Squadra::isAllenataDallUtente).count()).isEqualTo(1);
    }

    @Test
    void laSceltaDellaSquadraIgnoraMaiuscoleESpazi() {
        String nome = nomeSquadra(7, 2);
        NuovaCarrieraRichiesta richiesta = new NuovaCarrieraRichiesta("Carriera", "Mister", "  " + nome.toUpperCase() + " ", 7L);

        Carriera carriera = carrieraService.creaCarriera(mario.getId(), richiesta);

        assertThat(squadraRepository.findByCarrieraIdAndAllenataDallUtenteTrue(carriera.getId()).orElseThrow().getNome())
                .isEqualTo(nome);
    }

    @Test
    void lostessoSemeCreaLoStessoCampionato() {
        Carriera prima = carrieraService.creaCarriera(mario.getId(), richiesta(7, 1));
        Carriera seconda = carrieraService.creaCarriera(mario.getId(), richiesta(7, 1));
        em.flush();
        em.clear();

        assertThat(rosa(prima)).isEqualTo(rosa(seconda));
        assertThat(rosa(prima)).hasSize(260);
    }

    private List<String> rosa(Carriera carriera) {
        return giocatoreRepository.findAll().stream()
                .filter(giocatore -> giocatore.getCarriera().getId().equals(carriera.getId()))
                .map(giocatore -> giocatore.getSquadra().getNome() + " " + giocatore.nomeCompleto() + " "
                        + giocatore.getValutazione() + " " + giocatore.getNumeroMaglia())
                .sorted()
                .toList();
    }

    @Test
    void iGiocatoriSalvatiHannoAttributiCoerentiConLaValutazione() {
        Carriera carriera = carrieraService.creaCarriera(mario.getId(), richiesta(7, 0));
        em.flush();
        em.clear();

        for (Giocatore giocatore : giocatoreRepository.findAll()) {
            Attributi attributi = new Attributi(giocatore.getVelocita(), giocatore.getResistenza(), giocatore.getForza(),
                    giocatore.getTecnica(), giocatore.getPassaggio(), giocatore.getTiro(), giocatore.getDribbling(),
                    giocatore.getContrasto(), giocatore.getIntelligenza(), giocatore.getParate());

            assertThat(CalcolatoreAttributi.valutazione(giocatore.getPosizione(), attributi))
                    .as(giocatore.nomeCompleto()).isEqualTo(giocatore.getValutazione());
            assertThat(giocatore.getSquadra()).isNotNull();
            assertThat(giocatore.getCarriera().getId()).isEqualTo(carriera.getId());
        }
    }

    @Test
    void unaSquadraInesistenteNonCreaNiente() {
        NuovaCarrieraRichiesta richiesta = new NuovaCarrieraRichiesta("Carriera", "Mister", "Squadra Che Non Esiste", 7L);

        assertThatThrownBy(() -> carrieraService.creaCarriera(mario.getId(), richiesta))
                .isInstanceOf(OperazioneNonValidaException.class)
                .hasMessageContaining("Squadra Che Non Esiste");
        assertThat(carrieraRepository.count()).isZero();
        assertThat(giocatoreRepository.count()).isZero();
    }

    @Test
    void unUtenteInesistenteNonPuoCreareCarriere() {
        assertThatThrownBy(() -> carrieraService.creaCarriera(999_999L, richiesta(7, 0)))
                .isInstanceOf(RisorsaNonTrovataException.class);
    }

    @Test
    void siPuoAvereAlMassimoCinqueCarriere() {
        for (int i = 1; i <= 5; i++) {
            creaCarriera(mario, "Carriera " + i);
        }

        assertThatThrownBy(() -> carrieraService.creaCarriera(mario.getId(), richiesta(7, 0)))
                .isInstanceOf(OperazioneNonValidaException.class)
                .hasMessageContaining("massimo di 5");
    }

    @Test
    void ilLimiteDiCarriereEPerUtente() {
        Utente luca = creaUtente("luca");
        for (int i = 1; i <= 5; i++) {
            creaCarriera(mario, "Carriera " + i);
        }

        Carriera carriera = carrieraService.creaCarriera(luca.getId(), richiesta(7, 0));

        assertThat(carriera.getUtente().getId()).isEqualTo(luca.getId());
    }
}
