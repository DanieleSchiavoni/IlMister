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

import com.ilmister.dto.DecisioniIntervalloRichiesta;
import com.ilmister.dto.GiornataGiocataResponse;
import com.ilmister.dto.PrimoTempoResponse;
import com.ilmister.dto.SostituzioneRichiesta;
import com.ilmister.dto.NuovaCarrieraRichiesta;
import com.ilmister.dto.RigaClassificaResponse;
import com.ilmister.entity.Carriera;
import com.ilmister.entity.Giocatore;
import com.ilmister.entity.Utente;
import com.ilmister.entity.enumerazioni.StatoPartita;
import com.ilmister.entity.enumerazioni.TipoMovimento;
import com.ilmister.entity.enumerazioni.StatoStagione;
import com.ilmister.exception.OperazioneNonValidaException;
import com.ilmister.exception.RisorsaNonTrovataException;
import com.ilmister.generazione.ConfigurazioneGenerazione;
import com.ilmister.generazione.GeneratoreCampionato;
import com.ilmister.motore.MotorePartita;
import com.ilmister.repository.CarrieraRepository;
import com.ilmister.repository.EventoPartitaRepository;
import com.ilmister.repository.GiocatoreRepository;
import com.ilmister.repository.MovimentoFinanziarioRepository;
import com.ilmister.repository.PartitaRepository;
import com.ilmister.repository.SquadraRepository;
import com.ilmister.repository.PrestazioneGiocatoreRepository;
import com.ilmister.repository.RepositoryTestBase;
import com.ilmister.repository.StagioneRepository;
import com.ilmister.repository.StatisticaPartitaRepository;
import com.ilmister.service.impl.AvanzamentoServiceImpl;
import com.ilmister.service.impl.CarrieraServiceImpl;
import com.ilmister.service.impl.ClassificaServiceImpl;

@Import({ CarrieraServiceImpl.class, AvanzamentoServiceImpl.class, ClassificaServiceImpl.class,
        GeneratoreCampionato.class, MotorePartita.class })
class AvanzamentoServiceImplTest extends RepositoryTestBase {

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
    private AvanzamentoService avanzamentoService;
    @Autowired
    private ClassificaService classificaService;
    @Autowired
    private GeneratoreCampionato generatore;
    @Autowired
    private CarrieraRepository carrieraRepository;
    @Autowired
    private StagioneRepository stagioneRepository;
    @Autowired
    private PartitaRepository partitaRepository;
    @Autowired
    private EventoPartitaRepository eventoPartitaRepository;
    @Autowired
    private StatisticaPartitaRepository statisticaPartitaRepository;
    @Autowired
    private PrestazioneGiocatoreRepository prestazioneGiocatoreRepository;
    @Autowired
    private GiocatoreRepository giocatoreRepository;
    @Autowired
    private MovimentoFinanziarioRepository movimentoRepository;
    @Autowired
    private SquadraRepository squadraRepository;

    private Utente mario;

    @BeforeEach
    void preparaDati() {
        mario = creaUtente("mario");
    }

    private Carriera nuovaCarriera(Utente utente, long seme) {
        String squadra = generatore.genera(seme, ConfigurazioneGenerazione.predefinita(), LocalDate.of(2026, 7, 1))
                .squadre().get(0).nome();
        Carriera carriera = carrieraService.creaCarriera(utente.getId(),
                new NuovaCarrieraRichiesta("Carriera", "Mister Prova", squadra, seme));
        em.flush();
        em.clear();
        return carriera;
    }

    @Test
    void laGiornataGiocaTuttiICinqueIncontri() {
        Carriera carriera = nuovaCarriera(mario, 7);

        GiornataGiocataResponse giornata = avanzamentoService.avanza(carriera.getId(), mario.getId());

        assertThat(giornata.numero()).isEqualTo(1);
        assertThat(giornata.risultati()).hasSize(5);
        assertThat(giornata.stagioneConclusa()).isFalse();
        assertThat(giornata.risultati().stream().filter(r -> r.dellUtente()).count()).isEqualTo(1);
        em.flush();
        em.clear();
        assertThat(partitaRepository.findAll().stream().filter(p -> p.getStato() == StatoPartita.CONCLUSA)).hasSize(5);
    }

    @Test
    void siSalvanoCronacaStatisticheEPrestazioni() {
        Carriera carriera = nuovaCarriera(mario, 7);

        avanzamentoService.avanza(carriera.getId(), mario.getId());
        em.flush();
        em.clear();

        assertThat(statisticaPartitaRepository.count()).isEqualTo(10);
        assertThat(eventoPartitaRepository.count()).isGreaterThan(5 * 20);
        assertThat(prestazioneGiocatoreRepository.count()).isGreaterThanOrEqualTo(5 * 22);
        for (var partita : partitaRepository.findAll().stream().filter(p -> p.getStato() == StatoPartita.CONCLUSA).toList()) {
            assertThat(partita.getGolCasa()).isNotNull();
            assertThat(partita.getGolTrasferta()).isNotNull();
            assertThat(partita.getSpettatori()).isBetween(1, partita.getSquadraCasa().getCapienza());
            var gol = prestazioneGiocatoreRepository.findByPartitaId(partita.getId()).stream()
                    .mapToInt(prestazione -> prestazione.getGol()).sum();
            // I gol dei giocatori coincidono con il risultato (gli autogol, se ci sono, non sono dei marcatori)
            assertThat(gol).isLessThanOrEqualTo(partita.getGolCasa() + partita.getGolTrasferta());
        }
    }

    @Test
    void laClassificaSiAggiornaECoincideConIRisultati() {
        Carriera carriera = nuovaCarriera(mario, 7);
        GiornataGiocataResponse giornata = avanzamentoService.avanza(carriera.getId(), mario.getId());
        em.flush();
        em.clear();

        List<RigaClassificaResponse> classifica = classificaService.classifica(carriera.getId(), mario.getId());

        assertThat(classifica).hasSize(10);
        assertThat(classifica).allMatch(riga -> riga.giocate() == 1);
        assertThat(classifica.stream().mapToInt(RigaClassificaResponse::golFatti).sum())
                .isEqualTo(giornata.risultati().stream().mapToInt(r -> r.golCasa() + r.golTrasferta()).sum());
        assertThat(classifica.stream().mapToInt(RigaClassificaResponse::golFatti).sum())
                .isEqualTo(classifica.stream().mapToInt(RigaClassificaResponse::golSubiti).sum());
        assertThat(classifica.stream().mapToInt(RigaClassificaResponse::posizione)).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        // Ordinata per punti
        for (int i = 1; i < classifica.size(); i++) {
            assertThat(classifica.get(i - 1).punti()).isGreaterThanOrEqualTo(classifica.get(i).punti());
        }
        assertThat(classifica.stream().filter(RigaClassificaResponse::dellUtente)).hasSize(1);
        // Ogni partita assegna 3 punti (vittoria) o 2 (pareggio)
        long pareggi = giornata.risultati().stream().filter(r -> r.golCasa() == r.golTrasferta()).count();
        assertThat(classifica.stream().mapToInt(RigaClassificaResponse::punti).sum())
                .isEqualTo((int) (3 * (5 - pareggi) + 2 * pareggi));
    }

    @Test
    void laCarrieraAvanzaDiDataEPassaALaGiornataSuccessiva() {
        Carriera carriera = nuovaCarriera(mario, 7);

        GiornataGiocataResponse prima = avanzamentoService.avanza(carriera.getId(), mario.getId());
        GiornataGiocataResponse seconda = avanzamentoService.avanza(carriera.getId(), mario.getId());
        em.flush();
        em.clear();

        assertThat(prima.numero()).isEqualTo(1);
        assertThat(seconda.numero()).isEqualTo(2);
        assertThat(seconda.data()).isAfter(prima.data());
        assertThat(carrieraRepository.findById(carriera.getId()).orElseThrow().getDataCorrente()).isEqualTo(seconda.data());
    }

    @Test
    void conDiciottoGiornateLaStagioneEConclusa() {
        Carriera carriera = nuovaCarriera(mario, 7);

        GiornataGiocataResponse ultima = null;
        for (int i = 1; i <= 18; i++) {
            ultima = avanzamentoService.avanza(carriera.getId(), mario.getId());
            em.flush();
            em.clear();
        }

        assertThat(ultima.numero()).isEqualTo(18);
        assertThat(ultima.stagioneConclusa()).isTrue();
        assertThat(stagioneRepository.findByCarrieraIdOrderByAnnoInizioDesc(carriera.getId()).get(0).getStato())
                .isEqualTo(StatoStagione.CONCLUSA);
        assertThat(classificaService.classifica(carriera.getId(), mario.getId()))
                .allMatch(riga -> riga.giocate() == 18 && riga.vittorie() + riga.pareggi() + riga.sconfitte() == 18);
        assertThatThrownBy(() -> avanzamentoService.avanza(carriera.getId(), mario.getId()))
                .isInstanceOf(OperazioneNonValidaException.class);
    }

    @Test
    void nessunoPuoFarAvanzareLaCarrieraDiUnAltro() {
        Carriera carriera = nuovaCarriera(mario, 7);
        Utente luca = creaUtente("luca");

        assertThatThrownBy(() -> avanzamentoService.avanza(carriera.getId(), luca.getId()))
                .isInstanceOf(RisorsaNonTrovataException.class);
        assertThatThrownBy(() -> classificaService.classifica(carriera.getId(), luca.getId()))
                .isInstanceOf(RisorsaNonTrovataException.class);
    }

    @Test
    void lostessoSemeDaLaStessaClassifica() {
        Carriera prima = nuovaCarriera(mario, 7);
        Carriera seconda = nuovaCarriera(mario, 7);
        for (int i = 0; i < 3; i++) {
            avanzamentoService.avanza(prima.getId(), mario.getId());
            avanzamentoService.avanza(seconda.getId(), mario.getId());
        }
        em.flush();
        em.clear();

        assertThat(sintesi(classificaService.classifica(prima.getId(), mario.getId())))
                .isEqualTo(sintesi(classificaService.classifica(seconda.getId(), mario.getId())));
    }

    private List<String> sintesi(List<RigaClassificaResponse> classifica) {
        return classifica.stream()
                .map(riga -> riga.squadra() + " " + riga.punti() + " " + riga.golFatti() + "-" + riga.golSubiti())
                .toList();
    }

    @Test
    void lEnergiaDeiGiocatoriRestaTraZeroECento() {
        Carriera carriera = nuovaCarriera(mario, 7);
        for (int i = 0; i < 5; i++) {
            avanzamentoService.avanza(carriera.getId(), mario.getId());
        }
        em.flush();
        em.clear();

        for (Giocatore giocatore : giocatoreRepository.findAll()) {
            assertThat(giocatore.getEnergia()).isBetween(0, 100);
            assertThat(giocatore.getForma()).isBetween(1, 100);
            assertThat(giocatore.getMorale()).isBetween(1, 100);
            assertThat(giocatore.getSqualificaGiornate()).isGreaterThanOrEqualTo(0);
        }
    }

    // ---------- Il primo tempo e l'intervallo ----------

    @Test
    void ilPrimoTempoFermaLaPartitaDellUtenteAllIntervallo() {
        Carriera carriera = nuovaCarriera(mario, 7);

        PrimoTempoResponse primoTempo = avanzamentoService.giocaPrimoTempo(carriera.getId(), mario.getId());
        em.flush();
        em.clear();

        var partita = partitaRepository.findById(primoTempo.partitaId()).orElseThrow();
        assertThat(partita.getStato()).isEqualTo(StatoPartita.INTERVALLO);
        assertThat(partita.getStatoSimulazione()).isNotBlank();
        assertThat(partita.getGolCasa()).isEqualTo(primoTempo.golCasa());
        assertThat(primoTempo.cronaca()).isNotEmpty();
        assertThat(primoTempo.cronaca().get(primoTempo.cronaca().size() - 1)).contains("45");
        assertThat(primoTempo.giocatoriUtente()).hasSize(18);
        assertThat(primoTempo.giocatoriUtente().stream().filter(g -> g.inCampo()).count()).isBetween(9L, 11L);
        // Le altre partite non sono state toccate
        assertThat(partitaRepository.findAll().stream().filter(p -> p.getStato() != StatoPartita.DA_GIOCARE)).hasSize(1);
    }

    @Test
    void chiedereDiNuovoIlPrimoTempoDaLoStessoRisultato() {
        Carriera carriera = nuovaCarriera(mario, 7);

        PrimoTempoResponse prima = avanzamentoService.giocaPrimoTempo(carriera.getId(), mario.getId());
        em.flush();
        em.clear();
        PrimoTempoResponse seconda = avanzamentoService.giocaPrimoTempo(carriera.getId(), mario.getId());

        assertThat(seconda).isEqualTo(prima);
    }

    @Test
    void conUnaPartitaAllIntervalloNonSiPuoAvanzareDiColpo() {
        Carriera carriera = nuovaCarriera(mario, 7);
        avanzamentoService.giocaPrimoTempo(carriera.getId(), mario.getId());
        em.flush();
        em.clear();

        assertThatThrownBy(() -> avanzamentoService.avanza(carriera.getId(), mario.getId()))
                .isInstanceOf(OperazioneNonValidaException.class).hasMessageContaining("intervallo");
    }

    @Test
    void senzaPrimoTempoNonSiPuoConcludere() {
        Carriera carriera = nuovaCarriera(mario, 7);

        assertThatThrownBy(() -> avanzamentoService.concludiPartita(carriera.getId(), mario.getId(),
                new DecisioniIntervalloRichiesta(null, List.of())))
                .isInstanceOf(OperazioneNonValidaException.class);
    }

    @Test
    void siConcludeLaPartitaEIlPrimoTempoRestaQuelloMostrato() {
        Carriera carriera = nuovaCarriera(mario, 7);
        PrimoTempoResponse primoTempo = avanzamentoService.giocaPrimoTempo(carriera.getId(), mario.getId());
        em.flush();
        em.clear();

        GiornataGiocataResponse giornata = avanzamentoService.concludiPartita(carriera.getId(), mario.getId(),
                new DecisioniIntervalloRichiesta(null, List.of()));
        em.flush();
        em.clear();

        assertThat(giornata.risultati()).hasSize(5);
        var partita = partitaRepository.findById(primoTempo.partitaId()).orElseThrow();
        assertThat(partita.getStato()).isEqualTo(StatoPartita.CONCLUSA);
        assertThat(partita.getStatoSimulazione()).isNull();
        // I gol del primo tempo non spariscono nel risultato finale
        assertThat(partita.getGolCasa()).isGreaterThanOrEqualTo(primoTempo.golCasa());
        assertThat(partita.getGolTrasferta()).isGreaterThanOrEqualTo(primoTempo.golTrasferta());
        assertThat(partitaRepository.findAll().stream().filter(p -> p.getStato() == StatoPartita.CONCLUSA)).hasSize(5);
        assertThat(classificaService.classifica(carriera.getId(), mario.getId())).allMatch(riga -> riga.giocate() == 1);
    }

    @Test
    void ilCambioDellIntervalloSiVedeNellePrestazioni() {
        Carriera carriera = nuovaCarriera(mario, 7);
        PrimoTempoResponse primoTempo = avanzamentoService.giocaPrimoTempo(carriera.getId(), mario.getId());
        Long esce = primoTempo.giocatoriUtente().stream()
                .filter(g -> g.inCampo() && !g.siglaPosizione().equals("POR")).findFirst().orElseThrow().giocatoreId();
        Long entra = primoTempo.giocatoriUtente().stream()
                .filter(g -> !g.inCampo() && !g.espulso() && !g.infortunato() && !g.siglaPosizione().equals("POR"))
                .findFirst().orElseThrow().giocatoreId();
        em.flush();
        em.clear();

        avanzamentoService.concludiPartita(carriera.getId(), mario.getId(),
                new DecisioniIntervalloRichiesta(null, List.of(new SostituzioneRichiesta(esce, entra))));
        em.flush();
        em.clear();

        var prestazioni = prestazioneGiocatoreRepository.findByPartitaId(primoTempo.partitaId());
        var uscito = prestazioni.stream().filter(p -> p.getGiocatore().getId().equals(esce)).findFirst().orElseThrow();
        var entrato = prestazioni.stream().filter(p -> p.getGiocatore().getId().equals(entra)).findFirst().orElseThrow();
        assertThat(uscito.getMinutiGiocati()).isBetween(1, 89);
        assertThat(entrato.isTitolare()).isFalse();
        assertThat(entrato.getMinutiGiocati()).isGreaterThan(0);
    }

    @Test
    void unCambioNonValidoNonSalvaNiente() {
        Carriera carriera = nuovaCarriera(mario, 7);
        avanzamentoService.giocaPrimoTempo(carriera.getId(), mario.getId());
        em.flush();
        em.clear();

        assertThatThrownBy(() -> avanzamentoService.concludiPartita(carriera.getId(), mario.getId(),
                new DecisioniIntervalloRichiesta(null, List.of(new SostituzioneRichiesta(999_999L, 999_998L)))))
                .isInstanceOf(OperazioneNonValidaException.class);
    }

    // ---------- economia e allenamento ----------

    @Test
    void dopoLaGiornataSiPaganoGliStipendiESiIncassanoIBiglietti() {
        Carriera carriera = nuovaCarriera(mario, 7);
        var squadra = squadraRepository.findByCarrieraIdAndAllenataDallUtenteTrue(carriera.getId()).orElseThrow();
        java.math.BigDecimal budgetPrima = squadra.getBudgetTrasferimenti();
        java.math.BigDecimal monteStipendi = giocatoreRepository.findBySquadraIdOrderByCognomeAscNomeAsc(squadra.getId())
                .stream().map(Giocatore::getStipendioAnnuo).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        avanzamentoService.avanza(carriera.getId(), mario.getId());
        em.flush();
        em.clear();

        var movimenti = movimentoRepository.findTop30BySquadraIdOrderByDataMovimentoDescIdDesc(squadra.getId());
        var stipendi = movimenti.stream().filter(m -> m.getTipo() == TipoMovimento.STIPENDI).toList();
        assertThat(stipendi).hasSize(1);
        assertThat(stipendi.get(0).getImporto())
                .isEqualByComparingTo(monteStipendi.divide(java.math.BigDecimal.valueOf(18), 2, java.math.RoundingMode.HALF_UP).negate());
        // I biglietti si incassano solo giocando in casa
        long biglietti = movimenti.stream().filter(m -> m.getTipo() == TipoMovimento.BIGLIETTI).count();
        assertThat(biglietti).isLessThanOrEqualTo(1);
        // Il budget cambia esattamente della somma dei movimenti
        java.math.BigDecimal somma = movimenti.stream().map(m -> m.getImporto()).reduce(java.math.BigDecimal.ZERO,
                java.math.BigDecimal::add);
        assertThat(squadraRepository.findById(squadra.getId()).orElseThrow().getBudgetTrasferimenti())
                .isEqualByComparingTo(budgetPrima.add(somma));
    }

    @Test
    void ancheLeSquadreDelComputerPaganoStipendiEIncassano() {
        Carriera carriera = nuovaCarriera(mario, 7);
        var squadre = squadraRepository.findByCarrieraId(carriera.getId());
        var budgetPrima = new java.util.HashMap<Long, java.math.BigDecimal>();
        squadre.forEach(s -> budgetPrima.put(s.getId(), s.getBudgetTrasferimenti()));

        avanzamentoService.avanza(carriera.getId(), mario.getId());
        em.flush();
        em.clear();

        long cambiate = squadraRepository.findByCarrieraId(carriera.getId()).stream()
                .filter(s -> s.getBudgetTrasferimenti().compareTo(budgetPrima.get(s.getId())) != 0).count();
        assertThat(cambiate).isEqualTo(10);
    }

    @Test
    void conUnaStagioneDiAllenamentoLaSquadraDellUtenteMigliora() {
        Carriera carriera = nuovaCarriera(mario, 7);
        var squadra = squadraRepository.findByCarrieraIdAndAllenataDallUtenteTrue(carriera.getId()).orElseThrow();
        int prima = giocatoreRepository.findBySquadraIdOrderByCognomeAscNomeAsc(squadra.getId()).stream()
                .mapToInt(Giocatore::getValutazione).sum();

        for (int giornata = 1; giornata <= 18; giornata++) {
            avanzamentoService.avanza(carriera.getId(), mario.getId());
            em.flush();
            em.clear();
        }

        var rosa = giocatoreRepository.findBySquadraIdOrderByCognomeAscNomeAsc(squadra.getId());
        assertThat(rosa.stream().mapToInt(Giocatore::getValutazione).sum()).isGreaterThan(prima);
        assertThat(rosa).allSatisfy(g -> {
            assertThat(g.getValutazione()).isLessThanOrEqualTo(g.getPotenziale());
            assertThat(g.getVelocita()).isBetween(1, 99);
            assertThat(g.getTiro()).isBetween(1, 99);
        });
    }

    @Test
    void soloLaSquadraDellUtenteSiAllena() {
        Carriera carriera = nuovaCarriera(mario, 7);
        var altre = squadraRepository.findByCarrieraId(carriera.getId()).stream().filter(s -> !s.isAllenataDallUtente())
                .map(s -> s.getId()).toList();
        int prima = giocatoreRepository.findAll().stream().filter(g -> g.getSquadra() != null
                && altre.contains(g.getSquadra().getId())).mapToInt(Giocatore::getValutazione).sum();

        for (int giornata = 1; giornata <= 6; giornata++) {
            avanzamentoService.avanza(carriera.getId(), mario.getId());
            em.flush();
            em.clear();
        }

        int dopo = giocatoreRepository.findAll().stream().filter(g -> g.getSquadra() != null
                && altre.contains(g.getSquadra().getId())).mapToInt(Giocatore::getValutazione).sum();
        assertThat(dopo).isEqualTo(prima);
    }
}
