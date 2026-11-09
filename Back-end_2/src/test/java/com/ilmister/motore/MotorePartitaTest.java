package com.ilmister.motore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.ilmister.entity.enumerazioni.Mentalita;
import com.ilmister.entity.enumerazioni.Modulo;
import com.ilmister.entity.enumerazioni.TipoEvento;
import com.ilmister.generazione.SquadraGenerata;

class MotorePartitaTest {

    private final MotorePartita motore = new MotorePartita();
    private final List<SquadraGenerata> squadre = SquadreDiProva.campionato(7);

    private DatiPartita partita(long seme) {
        return SquadreDiProva.partita(seme, squadre.get(0), squadre.get(1));
    }

    private long contaEventi(RisultatoPartita risultato, TipoEvento tipo, Lato lato) {
        return risultato.eventi().stream().filter(e -> e.tipo() == tipo && e.lato() == lato).count();
    }

    // ---------- Ripetibilità ----------

    @Test
    void conGliStessiDatiELoStessoSemeLaPartitaEIdentica() {
        assertThat(motore.giocaPartita(partita(123))).isEqualTo(motore.giocaPartita(partita(123)));
    }

    @Test
    void conSemiDiversiLePartiteSonoDiverse() {
        Set<String> cronache = new HashSet<>();
        for (long seme = 1; seme <= 20; seme++) {
            cronache.add(motore.giocaPartita(partita(seme)).eventi().toString());
        }

        assertThat(cronache).hasSize(20);
    }

    @Test
    void ilPrimoTempoDellaPartitaIntieraEIdenticoAQuelloMostratoPrima() {
        for (long seme = 1; seme <= 25; seme++) {
            RisultatoPrimoTempo primoTempo = motore.primoTempo(partita(seme));
            RisultatoPartita intera = motore.giocaPartita(partita(seme));

            // Qualunque cosa si decida all'intervallo, il primo tempo non cambia
            assertThat(intera.eventi().subList(0, primoTempo.eventi().size())).isEqualTo(primoTempo.eventi());
        }
    }

    // ---------- Struttura della cronaca ----------

    @Test
    void laCronacaHaInizioIntervalloEFineAlPostoGiusto() {
        for (long seme = 1; seme <= 25; seme++) {
            List<EventoSimulato> eventi = motore.giocaPartita(partita(seme)).eventi();

            assertThat(eventi.get(0).tipo()).isEqualTo(TipoEvento.INIZIO_PARTITA);
            assertThat(eventi.get(eventi.size() - 1).tipo()).isEqualTo(TipoEvento.FINE_PARTITA);
            assertThat(eventi.stream().filter(e -> e.tipo() == TipoEvento.INTERVALLO).count()).isEqualTo(1L);
            assertThat(eventi.stream().filter(e -> e.tipo() == TipoEvento.INIZIO_SECONDO_TEMPO).count()).isEqualTo(1L);
        }
    }

    @Test
    void glieventiHannoOrdineProgressivoEMinutiNonDecrescenti() {
        for (long seme = 1; seme <= 25; seme++) {
            List<EventoSimulato> eventi = motore.giocaPartita(partita(seme)).eventi();
            int minutoPrecedente = 0;
            for (int i = 0; i < eventi.size(); i++) {
                EventoSimulato evento = eventi.get(i);
                assertThat(evento.ordine()).as("ordine seme " + seme).isEqualTo(i + 1);
                int minuto = evento.minuto() * 10 + evento.minutoRecupero();
                assertThat(minuto).as("minuti seme " + seme + " evento " + evento.testo()).isGreaterThanOrEqualTo(minutoPrecedente);
                minutoPrecedente = minuto;
                assertThat(evento.minuto()).isBetween(1, 90);
            }
        }
    }

    @Test
    void ogniEventoHaUnTestoSenzaSegnapostoRimasti() {
        for (long seme = 1; seme <= 40; seme++) {
            for (EventoSimulato evento : motore.giocaPartita(partita(seme)).eventi()) {
                assertThat(evento.testo().isBlank()).as(evento.tipo().name()).isFalse();
                assertThat(evento.testo().contains("%")).as(evento.testo()).isFalse();
                assertThat(evento.testo().contains("null")).as(evento.testo()).isFalse();
            }
        }
    }

    @Test
    void laXgCompareSoloNelleConclusioniAReteEStaTraZeroEUno() {
        Set<TipoEvento> conclusioni = Set.of(TipoEvento.GOL, TipoEvento.TIRO_PARATO, TipoEvento.TIRO_FUORI,
                TipoEvento.TIRO_BLOCCATO, TipoEvento.PALO);
        for (long seme = 1; seme <= 40; seme++) {
            for (EventoSimulato evento : motore.giocaPartita(partita(seme)).eventi()) {
                if (conclusioni.contains(evento.tipo())) {
                    assertThat(evento.xg()).as(evento.tipo().name()).isNotNull();
                    assertThat(evento.xg().doubleValue()).isBetween(0.0, 1.0);
                } else {
                    assertThat(evento.xg()).as(evento.tipo().name()).isNull();
                }
            }
        }
    }

    @Test
    void ogniGolDellaCronacaMostraIlRisultatoDelMomento() {
        for (long seme = 1; seme <= 40; seme++) {
            RisultatoPartita r = motore.giocaPartita(partita(seme));
            int casa = 0;
            int trasferta = 0;
            for (EventoSimulato evento : r.eventi()) {
                if (evento.tipo() == TipoEvento.GOL) {
                    if (evento.lato() == Lato.CASA) {
                        casa++;
                    } else {
                        trasferta++;
                    }
                    assertThat(evento.testo().endsWith("(" + casa + "-" + trasferta + ")")).as(evento.testo()).isTrue();
                }
            }
        }
    }

    // ---------- Coerenza dei numeri ----------

    @Test
    void ilRisultatoEQuelloDeiGolDellaCronacaEDeiGiocatori() {
        for (long seme = 1; seme <= 60; seme++) {
            RisultatoPartita r = motore.giocaPartita(partita(seme));

            assertThat(contaEventi(r, TipoEvento.GOL, Lato.CASA)).as("gol casa seme " + seme).isEqualTo((long) r.golCasa());
            assertThat(contaEventi(r, TipoEvento.GOL, Lato.TRASFERTA)).isEqualTo((long) r.golTrasferta());
            for (Lato lato : Lato.values()) {
                int golGiocatori = r.prestazioni().stream().filter(p -> p.lato() == lato).mapToInt(PrestazioneSimulata::gol).sum();
                int assist = r.prestazioni().stream().filter(p -> p.lato() == lato).mapToInt(PrestazioneSimulata::assist).sum();
                int golSquadra = lato == Lato.CASA ? r.golCasa() : r.golTrasferta();
                assertThat(golGiocatori).isEqualTo(golSquadra);
                assertThat(assist).isLessThanOrEqualTo(golSquadra);
            }
        }
    }

    @Test
    void leStatisticheSonoCoerentiConLaCronaca() {
        for (long seme = 1; seme <= 60; seme++) {
            RisultatoPartita r = motore.giocaPartita(partita(seme));
            for (Lato lato : Lato.values()) {
                StatisticheSquadra s = r.statistiche(lato);
                int gol = lato == Lato.CASA ? r.golCasa() : r.golTrasferta();

                assertThat(s.tiriInPorta()).isGreaterThanOrEqualTo(gol);
                assertThat(s.tiri()).isGreaterThanOrEqualTo(s.tiriInPorta());
                assertThat(s.xg()).isGreaterThan(-0.01);
                assertThat((long) s.ammonizioni()).isEqualTo(contaEventi(r, TipoEvento.AMMONIZIONE, lato));
                assertThat((long) s.espulsioni()).isEqualTo(contaEventi(r, TipoEvento.ESPULSIONE, lato));
                assertThat(s.possesso()).isBetween(25, 75);
            }
            assertThat(r.statisticheCasa().possesso() + r.statisticheTrasferta().possesso()).isBetween(98, 102);
        }
    }

    @Test
    void iGiocatoriHannoMinutiVotiEdEnergiaPlausibili() {
        for (long seme = 1; seme <= 40; seme++) {
            RisultatoPartita r = motore.giocaPartita(partita(seme));
            for (PrestazioneSimulata p : r.prestazioni()) {
                assertThat(p.minutiGiocati()).isBetween(0, 90);
                assertThat(p.voto()).isBetween(3.0, 10.0);
                assertThat(p.energiaFinale()).isBetween(0, 100);
                if (p.titolare() && !p.espulso()) {
                    assertThat(p.energiaFinale()).as("un titolare e piu stanco di prima").isLessThan(100);
                }
            }
            // Ogni squadra schiera almeno undici giocatori: i titolari
            for (Lato lato : Lato.values()) {
                assertThat(r.prestazioni().stream().filter(p -> p.lato() == lato && p.titolare()).count()).isEqualTo(11L);
            }
        }
    }

    @Test
    void gliInfortuniHannoUnEventoEUnaDurataSensata() {
        int infortuniVisti = 0;
        for (long seme = 1; seme <= 200; seme++) {
            RisultatoPartita r = motore.giocaPartita(partita(seme));
            long eventiInfortunio = r.eventi().stream().filter(e -> e.tipo() == TipoEvento.INFORTUNIO).count();
            assertThat((long) r.infortuni().size()).isEqualTo(eventiInfortunio);
            for (InfortunioSimulato infortunio : r.infortuni()) {
                assertThat(infortunio.giorni()).isBetween(3, 150);
                assertThat(infortunio.tipo().isBlank()).isFalse();
            }
            infortuniVisti += r.infortuni().size();
        }
        assertThat(infortuniVisti).as("in 200 partite ci sono infortuni, ma non troppi").isBetween(20, 120);
    }

    @Test
    void iCambiSonoAlMassimoCinquePerSquadraEEntranoDallaPanchina() {
        for (long seme = 1; seme <= 60; seme++) {
            DatiPartita dati = partita(seme);
            RisultatoPartita r = motore.giocaPartita(dati);
            for (Lato lato : Lato.values()) {
                Set<Long> panchina = new HashSet<>();
                dati.squadra(lato).panchina().forEach(g -> panchina.add(g.id()));
                List<EventoSimulato> cambi = r.eventi().stream()
                        .filter(e -> e.tipo() == TipoEvento.SOSTITUZIONE && e.lato() == lato).toList();

                assertThat(cambi.size()).isLessThanOrEqualTo(Simulazione.MASSIMO_SOSTITUZIONI);
                for (EventoSimulato cambio : cambi) {
                    assertThat(panchina.contains(cambio.secondoGiocatoreId())).isTrue();
                }
            }
        }
    }

    @Test
    void unEspulsoNonGiocaPiuELaSuaPrestazioneEBassa() {
        int espulsioniViste = 0;
        for (long seme = 1; seme <= 300; seme++) {
            RisultatoPartita r = motore.giocaPartita(partita(seme));
            for (PrestazioneSimulata p : r.prestazioni()) {
                if (p.espulso()) {
                    espulsioniViste++;
                    assertThat(p.minutiGiocati()).isLessThanOrEqualTo(90);
                    if (p.gol() == 0 && p.assist() == 0) {
                        assertThat(p.voto()).as("un espulso senza gol ne assist").isLessThanOrEqualTo(6.5);
                    }
                }
            }
        }
        assertThat(espulsioniViste).as("ci sono espulsioni, ma rare").isBetween(10, 120);
    }

    // ---------- Decisioni all'intervallo ----------

    private DecisioniIntervallo cambio(DatiPartita dati, Lato lato, int posizioneTitolare, int posizionePanchina) {
        SquadraInCampo squadra = dati.squadra(lato);
        return DecisioniIntervallo.manuali(null, List.of(new Sostituzione(squadra.titolari().get(posizioneTitolare).id(),
                squadra.panchina().get(posizionePanchina).id())));
    }

    @Test
    void unCambioAllIntervalloComparePrimaDelSecondoTempoEIGiocatoriHannoMinutiGiusti() {
        DatiPartita dati = partita(5);
        long esce = dati.casa().titolari().get(9).id();
        long entra = dati.casa().panchina().get(6).id();

        RisultatoPartita r = motore.giocaPartita(dati, cambio(dati, Lato.CASA, 9, 6), null);

        EventoSimulato evento = r.eventi().stream()
                .filter(e -> e.tipo() == TipoEvento.SOSTITUZIONE && e.giocatoreId() == esce).findFirst().orElseThrow();
        assertThat(evento.secondoGiocatoreId()).isEqualTo(entra);
        assertThat(evento.minuto()).isEqualTo(46);
        PrestazioneSimulata uscito = r.prestazioni().stream().filter(p -> p.giocatoreId() == esce).findFirst().orElseThrow();
        PrestazioneSimulata entrato = r.prestazioni().stream().filter(p -> p.giocatoreId() == entra).findFirst().orElseThrow();
        assertThat(uscito.minutiGiocati()).isEqualTo(45);
        assertThat(entrato.minutiGiocati()).isGreaterThanOrEqualTo(44);
        assertThat(entrato.titolare()).isFalse();
    }

    @Test
    void unCambioNonValidoESegnalato() {
        DatiPartita dati = partita(5);
        long titolare = dati.casa().titolari().get(3).id();
        long altroTitolare = dati.casa().titolari().get(4).id();
        long riserva = dati.casa().panchina().get(0).id();

        assertThatThrownBy(() -> motore.giocaPartita(dati, DecisioniIntervallo.manuali(null,
                List.of(new Sostituzione(riserva, riserva))), null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("non è in campo");
        assertThatThrownBy(() -> motore.giocaPartita(dati, DecisioniIntervallo.manuali(null,
                List.of(new Sostituzione(titolare, altroTitolare))), null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("non è in panchina");
        assertThatThrownBy(() -> motore.giocaPartita(dati, DecisioniIntervallo.manuali(null,
                List.of(new Sostituzione(999_999, riserva))), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void siPossonoFareAlMassimoCinqueCambi() {
        DatiPartita dati = partita(5);
        List<Sostituzione> sei = new java.util.ArrayList<>();
        for (int i = 0; i < 6; i++) {
            sei.add(new Sostituzione(dati.casa().titolari().get(i + 1).id(), dati.casa().panchina().get(i).id()));
        }

        assertThatThrownBy(() -> motore.giocaPartita(dati, DecisioniIntervallo.manuali(null, sei), null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("al massimo 5");
    }

    @Test
    void nuoveIstruzioniAllIntervalloCambianoIlSecondoTempo() {
        Istruzioni offensive = Istruzioni.equilibrate().conMentalita(Mentalita.MOLTO_OFFENSIVA);
        Istruzioni difensive = Istruzioni.equilibrate().conMentalita(Mentalita.MOLTO_DIFENSIVA);
        int tiriOffensivi = 0;
        int tiriDifensivi = 0;
        for (long seme = 1; seme <= 150; seme++) {
            DatiPartita dati = SquadreDiProva.partita(seme, squadre.get(2), squadre.get(3));
            tiriOffensivi += motore.giocaPartita(dati, DecisioniIntervallo.manuali(offensive, List.of()), null)
                    .statisticheCasa().tiri();
            tiriDifensivi += motore.giocaPartita(dati, DecisioniIntervallo.manuali(difensive, List.of()), null)
                    .statisticheCasa().tiri();
        }

        assertThat(tiriOffensivi).isGreaterThan(tiriDifensivi);
    }

    // ---------- Taratura: i numeri devono somigliare al calcio vero ----------

    @Test
    void iNumeriTotaliSomiglianoAQuelliDelCalcioVero() {
        int partite = 0;
        double gol = 0;
        double tiri = 0;
        double gialli = 0;
        double rossi = 0;
        double golCasa = 0;
        double golTrasferta = 0;
        int vittorieCasa = 0;
        int pareggi = 0;
        for (long seme = 1; seme <= 12; seme++) {
            List<SquadraGenerata> campionato = SquadreDiProva.campionato(seme);
            for (int i = 0; i < campionato.size(); i++) {
                for (int j = 0; j < campionato.size(); j++) {
                    if (i == j) {
                        continue;
                    }
                    RisultatoPartita r = motore.giocaPartita(
                            SquadreDiProva.partita(seme * 1_000 + i * 20 + j, campionato.get(i), campionato.get(j)));
                    partite++;
                    gol += r.golCasa() + r.golTrasferta();
                    golCasa += r.golCasa();
                    golTrasferta += r.golTrasferta();
                    tiri += (r.statisticheCasa().tiri() + r.statisticheTrasferta().tiri()) / 2.0;
                    gialli += r.statisticheCasa().ammonizioni() + r.statisticheTrasferta().ammonizioni();
                    rossi += r.statisticheCasa().espulsioni() + r.statisticheTrasferta().espulsioni();
                    if (r.golCasa() > r.golTrasferta()) {
                        vittorieCasa++;
                    } else if (r.golCasa() == r.golTrasferta()) {
                        pareggi++;
                    }
                }
            }
        }

        assertThat(gol / partite).as("gol per partita").isBetween(2.3, 3.0);
        assertThat(golCasa).as("la squadra di casa segna di piu").isGreaterThan(golTrasferta);
        assertThat(100.0 * vittorieCasa / partite).as("vittorie in casa %").isBetween(38.0, 50.0);
        assertThat(100.0 * pareggi / partite).as("pareggi %").isBetween(20.0, 32.0);
        assertThat(tiri / partite).as("tiri per squadra").isBetween(8.5, 14.0);
        assertThat(gialli / partite).as("ammonizioni per partita").isBetween(2.5, 4.5);
        assertThat(rossi / partite).as("espulsioni per partita").isBetween(0.03, 0.30);
    }

    @Test
    void laSquadraMoltoPiuForteVinceSpessoMaNonSempre() {
        int partite = 0;
        int vittorie = 0;
        int sconfitte = 0;
        for (long seme = 1; seme <= 30; seme++) {
            List<SquadraGenerata> campionato = SquadreDiProva.campionato(seme);
            SquadraGenerata forte = campionato.stream().max((a, b) -> Double.compare(a.forzaMedia(), b.forzaMedia())).orElseThrow();
            SquadraGenerata debole = campionato.stream().min((a, b) -> Double.compare(a.forzaMedia(), b.forzaMedia())).orElseThrow();
            for (int giro = 0; giro < 6; giro++) {
                RisultatoPartita r = motore.giocaPartita(SquadreDiProva.partita(seme * 100 + giro, forte, debole));
                partite++;
                if (r.golCasa() > r.golTrasferta()) {
                    vittorie++;
                } else if (r.golCasa() < r.golTrasferta()) {
                    sconfitte++;
                }
            }
        }

        assertThat(100.0 * vittorie / partite).as("vittorie della piu forte, in casa").isBetween(55.0, 90.0);
        assertThat(sconfitte).as("a volte la piu debole vince").isGreaterThan(0);
    }

    @Test
    void ilModuloELeIstruzioniNonRompono() {
        for (Modulo modulo : Modulo.values()) {
            DatiPartita dati = new DatiPartita(11, "Stadio",
                    SquadreDiProva.squadra(squadre.get(4), modulo, Istruzioni.equilibrate(), 1_000),
                    SquadreDiProva.squadra(squadre.get(5), Modulo.M_4_4_2, Istruzioni.equilibrate(), 2_000));

            RisultatoPartita r = motore.giocaPartita(dati);

            assertThat(r.eventi().size()).as(modulo.getCodice()).isGreaterThan(20);
        }
    }
}
