package com.ilmister.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
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

import com.ilmister.dto.EsitoTrattativaResponse;
import com.ilmister.dto.GiocatoreMercatoResponse;
import com.ilmister.dto.NuovaCarrieraRichiesta;
import com.ilmister.entity.Carriera;
import com.ilmister.entity.Giocatore;
import com.ilmister.entity.Squadra;
import com.ilmister.entity.Utente;
import com.ilmister.entity.enumerazioni.Posizione;
import com.ilmister.entity.enumerazioni.Ruolo;
import com.ilmister.entity.enumerazioni.TipoMovimento;
import com.ilmister.exception.OperazioneNonValidaException;
import com.ilmister.exception.RisorsaNonTrovataException;
import com.ilmister.generazione.ConfigurazioneGenerazione;
import com.ilmister.generazione.GeneratoreCampionato;
import com.ilmister.repository.GiocatoreRepository;
import com.ilmister.repository.MovimentoFinanziarioRepository;
import com.ilmister.repository.RepositoryTestBase;
import com.ilmister.repository.SquadraRepository;
import com.ilmister.service.impl.CarrieraServiceImpl;
import com.ilmister.service.impl.MercatoServiceImpl;

@Import({ CarrieraServiceImpl.class, MercatoServiceImpl.class, GeneratoreCampionato.class })
class MercatoServiceImplTest extends RepositoryTestBase {

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
    private MercatoService mercato;
    @Autowired
    private GeneratoreCampionato generatore;
    @Autowired
    private SquadraRepository squadraRepository;
    @Autowired
    private GiocatoreRepository giocatoreRepository;
    @Autowired
    private MovimentoFinanziarioRepository movimentoRepository;

    private Utente mario;
    private Carriera carriera;

    @BeforeEach
    void preparaDati() {
        mario = creaUtente("mario");
        carriera = nuovaCarriera(mario, 7);
    }

    private Carriera nuovaCarriera(Utente utente, long seme) {
        String squadra = generatore.genera(seme, ConfigurazioneGenerazione.predefinita(), LocalDate.of(2026, 7, 1))
                .squadre().get(0).nome();
        Carriera creata = carrieraService.creaCarriera(utente.getId(),
                new NuovaCarrieraRichiesta("Carriera", "Mister Prova", squadra, seme));
        em.flush();
        em.clear();
        return creata;
    }

    private Squadra mia() {
        return squadraRepository.findByCarrieraIdAndAllenataDallUtenteTrue(carriera.getId()).orElseThrow();
    }

    /** Il giocatore più economico tra quelli delle altre squadre */
    private GiocatoreMercatoResponse ilPiuEconomico() {
        List<GiocatoreMercatoResponse> tutti = mercato.cerca(carriera.getId(), mario.getId(), null, null, null);
        return tutti.get(tutti.size() - 1);
    }

    // ---------- ricerca ----------

    @Test
    void laRicercaMostraSoloGiocatoriDelleAltreSquadreFinoASessanta() {
        List<GiocatoreMercatoResponse> risultati = mercato.cerca(carriera.getId(), mario.getId(), null, null, null);

        assertThat(risultati).hasSize(60);
        assertThat(risultati).noneMatch(g -> g.squadraId().equals(mia().getId()));
        assertThat(risultati).isSortedAccordingTo((a, b) -> Integer.compare(b.valutazione(), a.valutazione()));
    }

    @Test
    void laRicercaSiPuoFiltrarePerRuoloENome() {
        List<GiocatoreMercatoResponse> portieri = mercato.cerca(carriera.getId(), mario.getId(), Ruolo.PORTIERE, null,
                null);
        assertThat(portieri).isNotEmpty().allMatch(g -> g.ruolo().equals("PORTIERE"));

        String cognome = portieri.get(0).cognome();
        assertThat(mercato.cerca(carriera.getId(), mario.getId(), null, cognome.toUpperCase(), null))
                .isNotEmpty().allMatch(g -> (g.nome() + " " + g.cognome()).toLowerCase().contains(cognome.toLowerCase()));

        BigDecimal tetto = new BigDecimal("2000");
        assertThat(mercato.cerca(carriera.getId(), mario.getId(), null, null, tetto))
                .allMatch(g -> g.valoreMercato().compareTo(tetto) <= 0);
    }

    // ---------- acquisti ----------

    @Test
    void unaBuonaOffertaVieneAccettataEIlGiocatoreArriva() {
        GiocatoreMercatoResponse scelto = ilPiuEconomico();
        BigDecimal offerta = scelto.valoreMercato().multiply(new BigDecimal("1.5"));
        BigDecimal budgetPrima = mia().getBudgetTrasferimenti();
        BigDecimal budgetVenditrice = squadraRepository.findById(scelto.squadraId()).orElseThrow()
                .getBudgetTrasferimenti();

        EsitoTrattativaResponse esito = mercato.offri(carriera.getId(), mario.getId(), scelto.id(), offerta);
        em.flush();
        em.clear();

        assertThat(esito.conclusa()).isTrue();
        Giocatore giocatore = giocatoreRepository.findById(scelto.id()).orElseThrow();
        assertThat(giocatore.getSquadra().getId()).isEqualTo(mia().getId());
        assertThat(mia().getBudgetTrasferimenti()).isEqualByComparingTo(budgetPrima.subtract(offerta));
        assertThat(squadraRepository.findById(scelto.squadraId()).orElseThrow().getBudgetTrasferimenti())
                .isEqualByComparingTo(budgetVenditrice.add(offerta));
        assertThat(esito.budgetResiduo()).isEqualByComparingTo(budgetPrima.subtract(offerta));
        var movimenti = movimentoRepository.findTop30BySquadraIdOrderByDataMovimentoDescIdDesc(mia().getId());
        assertThat(movimenti).hasSize(1);
        assertThat(movimenti.get(0).getTipo()).isEqualTo(TipoMovimento.ACQUISTO);
        assertThat(movimenti.get(0).getImporto()).isEqualByComparingTo(offerta.negate());
    }

    @Test
    void ilGiocatoreArrivatoNonRubaIlNumeroDiMagliaDiUnCompagno() {
        GiocatoreMercatoResponse scelto = ilPiuEconomico();

        mercato.offri(carriera.getId(), mario.getId(), scelto.id(), scelto.valoreMercato().multiply(new BigDecimal("1.5")));
        em.flush();
        em.clear();

        List<Integer> numeri = giocatoreRepository.findBySquadraIdOrderByCognomeAscNomeAsc(mia().getId()).stream()
                .map(Giocatore::getNumeroMaglia).toList();
        assertThat(numeri).doesNotContainNull().doesNotHaveDuplicates();
    }

    @Test
    void unOffertaTroppoBassaVieneRifiutataESenzaConseguenze() {
        GiocatoreMercatoResponse scelto = ilPiuEconomico();
        BigDecimal budgetPrima = mia().getBudgetTrasferimenti();

        EsitoTrattativaResponse esito = mercato.offri(carriera.getId(), mario.getId(), scelto.id(),
                scelto.valoreMercato().multiply(new BigDecimal("0.5")));
        em.flush();
        em.clear();

        assertThat(esito.conclusa()).isFalse();
        assertThat(esito.messaggio()).contains("rifiuta");
        assertThat(giocatoreRepository.findById(scelto.id()).orElseThrow().getSquadra().getId())
                .isEqualTo(scelto.squadraId());
        assertThat(mia().getBudgetTrasferimenti()).isEqualByComparingTo(budgetPrima);
        assertThat(movimentoRepository.count()).isZero();
    }

    @Test
    void nonSiOffreOltreIlBudget() {
        GiocatoreMercatoResponse scelto = ilPiuEconomico();
        BigDecimal troppo = mia().getBudgetTrasferimenti().add(BigDecimal.ONE);

        assertThatThrownBy(() -> mercato.offri(carriera.getId(), mario.getId(), scelto.id(), troppo))
                .isInstanceOf(OperazioneNonValidaException.class).hasMessageContaining("budget");
    }

    @Test
    void nonSiCompraUnGiocatoreGiaProprio() {
        Giocatore mio = giocatoreRepository.findBySquadraIdOrderByCognomeAscNomeAsc(mia().getId()).get(0);

        assertThatThrownBy(() -> mercato.offri(carriera.getId(), mario.getId(), mio.getId(), BigDecimal.TEN))
                .isInstanceOf(OperazioneNonValidaException.class);
    }

    @Test
    void conLaRosaPienaNonSiCompra() {
        for (int i = 0; giocatoreRepository.countBySquadraId(mia().getId()) < 30; i++) {
            GiocatoreMercatoResponse scelto = mercato.cerca(carriera.getId(), mario.getId(), null, null, null)
                    .get(60 - 1 - i % 10);
            Giocatore giocatore = giocatoreRepository.findById(scelto.id()).orElseThrow();
            giocatore.setSquadra(mia());
            giocatore.setNumeroMaglia(null);
            em.flush();
            em.clear();
        }
        GiocatoreMercatoResponse scelto = ilPiuEconomico();

        assertThatThrownBy(() -> mercato.offri(carriera.getId(), mario.getId(), scelto.id(), BigDecimal.TEN))
                .isInstanceOf(OperazioneNonValidaException.class).hasMessageContaining("massimo");
    }

    // ---------- cessioni e svincoli ----------

    @Test
    void laVenditaPortaUnIncassoPariAlOttantacinquePerCentoDelValore() {
        Giocatore mio = giocatoreRepository.findBySquadraIdOrderByCognomeAscNomeAsc(mia().getId()).stream()
                .filter(g -> g.getRuolo() != Ruolo.PORTIERE).findFirst().orElseThrow();
        BigDecimal budgetPrima = mia().getBudgetTrasferimenti();
        BigDecimal valore = mio.getValoreMercato();

        EsitoTrattativaResponse esito = mercato.vendi(carriera.getId(), mario.getId(), mio.getId());
        em.flush();
        em.clear();

        assertThat(esito.conclusa()).isTrue();
        BigDecimal prezzo = mia().getBudgetTrasferimenti().subtract(budgetPrima);
        assertThat(prezzo).isPositive().isLessThan(valore);
        assertThat(giocatoreRepository.findById(mio.getId()).orElseThrow().getSquadra().getId())
                .isNotEqualTo(mia().getId());
        var movimenti = movimentoRepository.findTop30BySquadraIdOrderByDataMovimentoDescIdDesc(mia().getId());
        assertThat(movimenti).hasSize(1);
        assertThat(movimenti.get(0).getTipo()).isEqualTo(TipoMovimento.VENDITA);
        assertThat(movimenti.get(0).getImporto()).isEqualByComparingTo(prezzo);
    }

    @Test
    void nonSiVendeUnGiocatoreAltrui() {
        GiocatoreMercatoResponse altrui = ilPiuEconomico();

        assertThatThrownBy(() -> mercato.vendi(carriera.getId(), mario.getId(), altrui.id()))
                .isInstanceOf(OperazioneNonValidaException.class);
    }

    @Test
    void unGiocatoreSvincolatoSiVedeTraGliSvincolatiEPuoTornare() {
        Giocatore mio = giocatoreRepository.findBySquadraIdOrderByCognomeAscNomeAsc(mia().getId()).stream()
                .filter(g -> g.getRuolo() != Ruolo.PORTIERE).findFirst().orElseThrow();
        BigDecimal budgetPrima = mia().getBudgetTrasferimenti();

        mercato.svincola(carriera.getId(), mario.getId(), mio.getId());
        em.flush();
        em.clear();

        assertThat(giocatoreRepository.findById(mio.getId()).orElseThrow().getSquadra()).isNull();
        assertThat(mercato.svincolati(carriera.getId(), mario.getId())).extracting(GiocatoreMercatoResponse::id)
                .contains(mio.getId());
        assertThat(mia().getBudgetTrasferimenti()).isEqualByComparingTo(budgetPrima);

        mercato.ingaggia(carriera.getId(), mario.getId(), mio.getId());
        em.flush();
        em.clear();

        assertThat(giocatoreRepository.findById(mio.getId()).orElseThrow().getSquadra().getId()).isEqualTo(mia().getId());
        assertThat(mercato.svincolati(carriera.getId(), mario.getId())).isEmpty();
    }

    @Test
    void nonSiIngaggiaChiHaGiaUnaSquadra() {
        GiocatoreMercatoResponse altrui = ilPiuEconomico();

        assertThatThrownBy(() -> mercato.ingaggia(carriera.getId(), mario.getId(), altrui.id()))
                .isInstanceOf(OperazioneNonValidaException.class);
    }

    @Test
    void laRosaNonScendeSottoIlMinimo() {
        while (giocatoreRepository.countBySquadraId(mia().getId()) > 16) {
            Giocatore mio = giocatoreRepository.findBySquadraIdOrderByCognomeAscNomeAsc(mia().getId()).stream()
                    .filter(g -> g.getRuolo() != Ruolo.PORTIERE).findFirst().orElseThrow();
            mercato.svincola(carriera.getId(), mario.getId(), mio.getId());
            em.flush();
            em.clear();
        }
        Giocatore altro = giocatoreRepository.findBySquadraIdOrderByCognomeAscNomeAsc(mia().getId()).stream()
                .filter(g -> g.getRuolo() != Ruolo.PORTIERE).findFirst().orElseThrow();

        assertThatThrownBy(() -> mercato.svincola(carriera.getId(), mario.getId(), altro.getId()))
                .isInstanceOf(OperazioneNonValidaException.class).hasMessageContaining("16");
        assertThatThrownBy(() -> mercato.vendi(carriera.getId(), mario.getId(), altro.getId()))
                .isInstanceOf(OperazioneNonValidaException.class);
    }

    @Test
    void servonoSempreAlmenoDuePortieri() {
        List<Giocatore> portieri = giocatoreRepository.findBySquadraIdOrderByCognomeAscNomeAsc(mia().getId()).stream()
                .filter(g -> g.getPosizione() == Posizione.PORTIERE).toList();
        // La rosa generata ha più di due portieri: si arriva a due e poi ci si ferma
        for (int i = 0; i < portieri.size() - 2; i++) {
            mercato.svincola(carriera.getId(), mario.getId(), portieri.get(i).getId());
            em.flush();
            em.clear();
        }
        Giocatore ultimo = portieri.get(portieri.size() - 1);

        assertThatThrownBy(() -> mercato.svincola(carriera.getId(), mario.getId(), ultimo.getId()))
                .isInstanceOf(OperazioneNonValidaException.class).hasMessageContaining("portieri");
    }

    // ---------- proprietà della carriera ----------

    @Test
    void chiNonEIlProprietarioNonTrovaNiente() {
        Utente luca = creaUtente("luca");
        GiocatoreMercatoResponse scelto = ilPiuEconomico();

        assertThatThrownBy(() -> mercato.cerca(carriera.getId(), luca.getId(), null, null, null))
                .isInstanceOf(RisorsaNonTrovataException.class);
        assertThatThrownBy(() -> mercato.offri(carriera.getId(), luca.getId(), scelto.id(), BigDecimal.TEN))
                .isInstanceOf(RisorsaNonTrovataException.class);
        assertThatThrownBy(() -> mercato.svincolati(carriera.getId(), luca.getId()))
                .isInstanceOf(RisorsaNonTrovataException.class);
    }

    @Test
    void unGiocatoreInesistenteDa404() {
        assertThatThrownBy(() -> mercato.offri(carriera.getId(), mario.getId(), 999_999L, BigDecimal.TEN))
                .isInstanceOf(RisorsaNonTrovataException.class);
    }
}
