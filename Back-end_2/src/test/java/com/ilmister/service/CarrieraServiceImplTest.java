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
import com.ilmister.repository.ClassificaRepository;
import com.ilmister.repository.GiornataRepository;
import com.ilmister.repository.PartitaRepository;
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
    @Autowired
    private GiornataRepository giornataRepository;
    @Autowired
    private PartitaRepository partitaRepository;
    @Autowired
    private ClassificaRepository classificaRepository;

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

    @Test
    void creaIlCalendarioConAndataERitorno() {
        Carriera carriera = carrieraService.creaCarriera(mario.getId(), richiesta(7, 0));
        em.flush();
        em.clear();
        var stagione = stagioneRepository.findByCarrieraIdAndStato(carriera.getId(), StatoStagione.IN_CORSO).orElseThrow();
        var campionato = campionatoRepository.findByCarrieraIdOrderByLivelloAscNomeAsc(carriera.getId()).get(0);

        var giornate = giornataRepository.findByStagioneIdAndCampionatoIdOrderByNumeroAsc(stagione.getId(), campionato.getId());

        assertThat(giornate).hasSize(18);
        assertThat(giornate).extracting("numero").containsExactlyElementsOf(java.util.stream.IntStream.rangeClosed(1, 18).boxed().toList());
        assertThat(partitaRepository.count()).isEqualTo(90);
        for (var giornata : giornate) {
            assertThat(partitaRepository.findByGiornataId(giornata.getId())).hasSize(5)
                    .allMatch(partita -> partita.getStato() == com.ilmister.entity.enumerazioni.StatoPartita.DA_GIOCARE);
        }
        assertThat(giornate.get(0).getDataGiornata()).isAfter(stagione.getDataInizio());
    }

    @Test
    void ogniSquadraGiocaDiciottoPartiteNoveInCasa() {
        Carriera carriera = carrieraService.creaCarriera(mario.getId(), richiesta(7, 0));
        em.flush();
        em.clear();

        for (Squadra squadra : squadraRepository.findByCarrieraId(carriera.getId())) {
            long inCasa = partitaRepository.findAll().stream()
                    .filter(partita -> partita.getSquadraCasa().getId().equals(squadra.getId())).count();
            long inTrasferta = partitaRepository.findAll().stream()
                    .filter(partita -> partita.getSquadraTrasferta().getId().equals(squadra.getId())).count();
            assertThat(inCasa).as(squadra.getNome()).isEqualTo(9);
            assertThat(inTrasferta).as(squadra.getNome()).isEqualTo(9);
        }
    }

    @Test
    void laClassificaPartePerOgniSquadraDaZero() {
        Carriera carriera = carrieraService.creaCarriera(mario.getId(), richiesta(7, 0));
        em.flush();
        em.clear();
        var stagione = stagioneRepository.findByCarrieraIdAndStato(carriera.getId(), StatoStagione.IN_CORSO).orElseThrow();
        var campionato = campionatoRepository.findByCarrieraIdOrderByLivelloAscNomeAsc(carriera.getId()).get(0);

        var classifica = classificaRepository.classifica(stagione.getId(), campionato.getId());

        assertThat(classifica).hasSize(10);
        assertThat(classifica).allMatch(voce -> voce.getGiocate() == 0 && voce.getPunti() == 0);
    }

    @Test
    void lostessoSemeCreaLoStessoCalendario() {
        Carriera prima = carrieraService.creaCarriera(mario.getId(), richiesta(7, 1));
        Carriera seconda = carrieraService.creaCarriera(mario.getId(), richiesta(7, 1));
        em.flush();
        em.clear();

        assertThat(calendario(prima)).isEqualTo(calendario(seconda)).hasSize(90);
    }

    private List<String> calendario(Carriera carriera) {
        return partitaRepository.findAll().stream()
                .filter(partita -> partita.getSquadraCasa().getCarriera().getId().equals(carriera.getId()))
                .sorted(java.util.Comparator.comparingInt((com.ilmister.entity.Partita partita) -> partita.getGiornata().getNumero())
                        .thenComparing(partita -> partita.getSquadraCasa().getNome()))
                .map(partita -> partita.getGiornata().getNumero() + " " + partita.getSquadraCasa().getNome() + "-"
                        + partita.getSquadraTrasferta().getNome() + " " + partita.getSeme())
                .toList();
    }

    @Test
    void elencaLeCarriereDellUtenteConLaSquadraEIlCampionato() {
        Carriera creata = carrieraService.creaCarriera(mario.getId(), richiesta(7, 4));
        carrieraService.creaCarriera(creaUtente("luca").getId(), richiesta(7, 1));
        em.flush();
        em.clear();

        var carriere = carrieraService.carriere(mario.getId());

        assertThat(carriere).hasSize(1);
        var carriera = carriere.get(0);
        assertThat(carriera.id()).isEqualTo(creata.getId());
        assertThat(carriera.squadra()).isEqualTo(nomeSquadra(7, 4));
        assertThat(carriera.stagione()).isEqualTo("2026/27");
        assertThat(carriera.prossimaGiornata()).isEqualTo(1);
        assertThat(carriera.giornateTotali()).isEqualTo(18);
        assertThat(carriera.dataCorrente()).isEqualTo(LocalDate.of(2026, 7, 1));
    }

    @Test
    void ilDettaglioDiUnaCarrieraAltruiNonSiTrova() {
        Carriera creata = carrieraService.creaCarriera(mario.getId(), richiesta(7, 0));
        Utente luca = creaUtente("luca");
        em.flush();
        em.clear();

        assertThat(carrieraService.carriera(creata.getId(), mario.getId()).nome()).isEqualTo("La mia carriera");
        assertThatThrownBy(() -> carrieraService.carriera(creata.getId(), luca.getId()))
                .isInstanceOf(RisorsaNonTrovataException.class);
    }

    @Test
    void eliminaLaCarrieraSoloSeEDellUtente() {
        Carriera creata = carrieraService.creaCarriera(mario.getId(), richiesta(7, 0));
        Utente luca = creaUtente("luca");
        em.flush();
        em.clear();

        assertThatThrownBy(() -> carrieraService.elimina(creata.getId(), luca.getId()))
                .isInstanceOf(RisorsaNonTrovataException.class);
        assertThat(carrieraRepository.count()).isEqualTo(1);

        carrieraService.elimina(creata.getId(), mario.getId());
        em.flush();

        assertThat(carrieraRepository.count()).isZero();
    }
}
