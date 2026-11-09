package com.ilmister.motore;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SplittableRandom;

import com.ilmister.entity.enumerazioni.LineaDifensiva;
import com.ilmister.entity.enumerazioni.Mentalita;
import com.ilmister.entity.enumerazioni.Modulo;
import com.ilmister.entity.enumerazioni.Posizione;
import com.ilmister.entity.enumerazioni.Pressing;
import com.ilmister.entity.enumerazioni.Ritmo;
import com.ilmister.entity.enumerazioni.Ruolo;
import com.ilmister.entity.enumerazioni.TipoEvento;
import com.ilmister.generazione.CalcolatoreAttributi;

/**
 * Una partita in corso. Si gioca minuto per minuto: in ogni minuto ogni squadra può costruire un'azione offensiva
 * (la probabilità dipende dal controllo del gioco e dalle istruzioni), che può diventare un tiro; poi ci sono falli,
 * cartellini, infortuni e il calo di energia. Tutti i numeri casuali vengono dal seme della partita: il primo tempo
 * dipende solo dalle squadre e dal seme, il secondo anche dalle decisioni dell'intervallo.
 */
final class Simulazione {

    static final int MASSIMO_SOSTITUZIONI = 5;

    // Parametri di taratura (vedi i test di calibrazione)
    private static final double SEQUENZE_PER_MINUTO = 0.165;
    private static final double VANTAGGIO_CASA = 1.12;
    private static final double FALLI_PER_MINUTO = 0.24;
    private static final double INFORTUNI_PER_MINUTO = 0.0018;
    private static final double PROBABILITA_RIGORE = 0.010;
    private static final long SECONDO_SEME = 0x5DEECE66DL;

    // ---------- Stato ----------

    private static final class Posto {
        final Posizione posizione;
        Giocatore giocatore;

        Posto(Posizione posizione) {
            this.posizione = posizione;
        }
    }

    private static final class Giocatore {
        final GiocatoreInCampo dati;
        final Lato lato;
        final boolean titolare;
        final int valutazione;
        double energia;
        int ammonizioni;
        boolean espulso;
        boolean infortunato;
        boolean inCampo;
        boolean entrato;
        int minutoIngresso;
        int minutoUscita = -1;
        int gol;
        int assist;
        int tiri;
        int parate;
        Posto posto;

        Giocatore(GiocatoreInCampo dati, Lato lato, boolean titolare) {
            this.dati = dati;
            this.lato = lato;
            this.titolare = titolare;
            this.energia = dati.energia();
            this.valutazione = CalcolatoreAttributi.valutazione(dati.posizione(), dati.attributi());
        }
    }

    private static final class Squadra {
        final Lato lato;
        final SquadraInCampo dati;
        Istruzioni istruzioni;
        final Istruzioni istruzioniIniziali;
        final Posto[] posti;
        final List<Giocatore> tutti = new ArrayList<>();
        final Map<Long, Giocatore> perId = new HashMap<>();
        int sostituzioni;
        int tiri;
        int tiriInPorta;
        int falli;
        int angoli;
        int ammonizioni;
        int espulsioni;
        double xg;
        double controllo;
        int minutiControllo;

        Squadra(Lato lato, SquadraInCampo dati) {
            this.lato = lato;
            this.dati = dati;
            this.istruzioni = dati.istruzioni();
            this.istruzioniIniziali = dati.istruzioni();
            this.posti = new Posto[dati.titolari().size()];
            for (int i = 0; i < posti.length; i++) {
                Posto posto = new Posto(dati.modulo().getPosti().get(i));
                Giocatore giocatore = new Giocatore(dati.titolari().get(i), lato, true);
                giocatore.inCampo = true;
                giocatore.entrato = true;
                giocatore.posto = posto;
                posto.giocatore = giocatore;
                posti[i] = posto;
                registra(giocatore);
            }
            for (GiocatoreInCampo riserva : dati.panchina()) {
                registra(new Giocatore(riserva, lato, false));
            }
        }

        private void registra(Giocatore giocatore) {
            tutti.add(giocatore);
            perId.put(giocatore.dati.id(), giocatore);
        }

        List<Giocatore> inCampo() {
            List<Giocatore> lista = new ArrayList<>();
            for (Posto posto : posti) {
                if (posto.giocatore != null) {
                    lista.add(posto.giocatore);
                }
            }
            return lista;
        }

        List<Giocatore> panchina() {
            return tutti.stream().filter(g -> !g.titolare && !g.entrato).toList();
        }
    }

    private record Forze(double attacco, double creazione, double difesa, double portiere) {
    }

    private final DatiPartita dati;
    private SplittableRandom r;
    private final Squadra[] squadre = new Squadra[2];
    private final List<EventoSimulato> eventi = new ArrayList<>();
    private final List<InfortunioSimulato> infortuni = new ArrayList<>();
    private int golCasa;
    private int golTrasferta;
    private int minuto;
    private int recupero;
    private int ordine;
    private boolean primoTempoGiocato;
    private boolean secondoTempoGiocato;

    Simulazione(DatiPartita dati) {
        this.dati = dati;
        this.r = new SplittableRandom(dati.seme());
        squadre[Lato.CASA.ordinal()] = new Squadra(Lato.CASA, dati.casa());
        squadre[Lato.TRASFERTA.ordinal()] = new Squadra(Lato.TRASFERTA, dati.trasferta());
    }

    private Squadra squadra(Lato lato) {
        return squadre[lato.ordinal()];
    }

    private int gol(Lato lato) {
        return lato == Lato.CASA ? golCasa : golTrasferta;
    }

    // ---------- Primo e secondo tempo ----------

    void giocaPrimoTempo() {
        if (primoTempoGiocato) {
            throw new IllegalStateException("Il primo tempo è già stato giocato");
        }
        evento(TipoEvento.INIZIO_PARTITA, null, null, null, null,
                Cronaca.inizio(dati.casa().nome(), dati.trasferta().nome(), dati.stadio()), 1, 0);

        int eventiPrima = eventi.size();
        for (int m = 1; m <= 45; m++) {
            giocaMinuto(m, 0);
        }
        int recuperoPrimoTempo = Math.max(1, Math.min(4, 1 + (eventi.size() - eventiPrima) / 14 + r.nextInt(2)));
        for (int k = 1; k <= recuperoPrimoTempo; k++) {
            giocaMinuto(45, k);
        }
        evento(TipoEvento.INTERVALLO, null, null, null, null,
                Cronaca.intervallo(dati.casa().nome(), golCasa, golTrasferta, dati.trasferta().nome()), 45,
                recuperoPrimoTempo);
        primoTempoGiocato = true;
    }

    void applicaDecisioni(DecisioniIntervallo decisioniCasa, DecisioniIntervallo decisioniTrasferta) {
        if (!primoTempoGiocato) {
            throw new IllegalStateException("Il primo tempo non è ancora stato giocato");
        }
        // Il secondo tempo ha una sua serie di numeri casuali, che dipende solo dal seme
        r = new SplittableRandom(dati.seme() ^ SECONDO_SEME);
        evento(TipoEvento.INIZIO_SECONDO_TEMPO, null, null, null, null, Cronaca.inizioSecondoTempo(), 46, 0);

        inIntervallo = true;
        applicaDecisioni(squadra(Lato.CASA), decisioniCasa == null ? DecisioniIntervallo.automatiche() : decisioniCasa);
        applicaDecisioni(squadra(Lato.TRASFERTA),
                decisioniTrasferta == null ? DecisioniIntervallo.automatiche() : decisioniTrasferta);
        inIntervallo = false;
    }

    private final Map<Lato, DecisioniIntervallo> decisioni = new HashMap<>();
    private boolean inIntervallo;

    private void applicaDecisioni(Squadra squadra, DecisioniIntervallo scelte) {
        decisioni.put(squadra.lato, scelte);
        if (scelte.istruzioni() != null) {
            squadra.istruzioni = scelte.istruzioni();
        }
        for (Sostituzione cambio : scelte.sostituzioni()) {
            Giocatore esce = squadra.perId.get(cambio.esceId());
            Giocatore entra = squadra.perId.get(cambio.entraId());
            if (esce == null || !esce.inCampo || esce.espulso) {
                throw new IllegalArgumentException("Il giocatore che esce non è in campo");
            }
            if (entra == null || entra.entrato || entra.titolare) {
                throw new IllegalArgumentException("Il giocatore che entra non è in panchina");
            }
            if (squadra.sostituzioni >= MASSIMO_SOSTITUZIONI) {
                throw new IllegalArgumentException("Si possono fare al massimo " + MASSIMO_SOSTITUZIONI + " cambi");
            }
            minuto = 46;
            recupero = 0;
            eseguiSostituzione(squadra, esce, entra, false);
        }
        if (scelte.adattaMentalita()) {
            adattaMentalita(squadra);
        }
    }

    void giocaSecondoTempo() {
        if (!primoTempoGiocato) {
            throw new IllegalStateException("Il primo tempo non è ancora stato giocato");
        }
        if (secondoTempoGiocato) {
            throw new IllegalStateException("La partita è già finita");
        }
        if (decisioni.isEmpty()) {
            applicaDecisioni(DecisioniIntervallo.automatiche(), DecisioniIntervallo.automatiche());
        }
        int eventiPrima = eventi.size();
        for (int m = 46; m <= 90; m++) {
            if (m == 60 || m == 70 || m == 78) {
                for (Lato lato : Lato.values()) {
                    DecisioniIntervallo scelte = decisioni.get(lato);
                    minuto = m;
                    recupero = 0;
                    if (scelte.sostituzioniAutomatiche()) {
                        sostituzioniPerStanchezza(squadra(lato));
                    }
                    if (scelte.adattaMentalita() && (m == 70 || m == 78)) {
                        adattaMentalita(squadra(lato));
                    }
                }
            }
            giocaMinuto(m, 0);
        }
        int recuperoSecondoTempo = Math.max(2, Math.min(6, 2 + (eventi.size() - eventiPrima) / 14 + r.nextInt(2)));
        for (int k = 1; k <= recuperoSecondoTempo; k++) {
            giocaMinuto(90, k);
        }
        evento(TipoEvento.FINE_PARTITA, null, null, null, null,
                Cronaca.fine(dati.casa().nome(), golCasa, golTrasferta, dati.trasferta().nome()), 90,
                recuperoSecondoTempo);
        secondoTempoGiocato = true;
    }

    // ---------- Un minuto di gioco ----------

    private void giocaMinuto(int m, int rec) {
        minuto = m;
        recupero = rec;
        Squadra casa = squadra(Lato.CASA);
        Squadra trasferta = squadra(Lato.TRASFERTA);
        Forze forzeCasa = calcolaForze(casa);
        Forze forzeTrasferta = calcolaForze(trasferta);

        // Chi controlla il gioco: quota di creazione, con il vantaggio del campo e un po' di variazione
        double creazioneCasa = forzeCasa.creazione() * VANTAGGIO_CASA;
        double quotaCasa = creazioneCasa / (creazioneCasa + forzeTrasferta.creazione());
        quotaCasa = limita(quotaCasa + r.nextGaussian() * 0.03, 0.2, 0.8);
        casa.controllo += quotaCasa;
        trasferta.controllo += 1 - quotaCasa;
        casa.minutiControllo++;
        trasferta.minutiControllo++;

        boolean primaCasa = r.nextBoolean();
        Lato[] ordineLati = primaCasa ? new Lato[] { Lato.CASA, Lato.TRASFERTA } : new Lato[] { Lato.TRASFERTA, Lato.CASA };
        for (Lato lato : ordineLati) {
            Squadra attacco = squadra(lato);
            Squadra difesa = squadra(lato.avversario());
            double quota = lato == Lato.CASA ? quotaCasa : 1 - quotaCasa;
            double probabilita = SEQUENZE_PER_MINUTO * 2 * quota
                    * moltiplicatoreRitmo(attacco.istruzioni.ritmo())
                    * moltiplicatoreAttaccoMentalita(attacco.istruzioni.mentalita())
                    * moltiplicatoreConcessioneMentalita(difesa.istruzioni.mentalita())
                    * moltiplicatoreSequenzeDaPressing(difesa.istruzioni.pressing())
                    * moltiplicatoreSequenzeDaLinea(difesa.istruzioni.lineaDifensiva())
                    * moltiplicatoreSequenzeProprieDaPressing(attacco.istruzioni.pressing())
                    * moltiplicatoreSequenzeProprieDaLinea(attacco.istruzioni.lineaDifensiva());
            if (r.nextDouble() < probabilita) {
                Forze forzeAttacco = lato == Lato.CASA ? forzeCasa : forzeTrasferta;
                Forze forzeDifesa = lato == Lato.CASA ? forzeTrasferta : forzeCasa;
                double rapporto = forzeAttacco.attacco() * (lato == Lato.CASA ? VANTAGGIO_CASA : 1.0)
                        / Math.max(0.2, forzeDifesa.difesa());
                sequenza(attacco, difesa, rapporto);
            }
        }

        if (r.nextDouble() < FALLI_PER_MINUTO) {
            fallo(quotaCasa);
        }
        for (Lato lato : Lato.values()) {
            if (r.nextDouble() < INFORTUNI_PER_MINUTO) {
                infortunio(squadra(lato));
            }
        }
        consumaEnergia(casa);
        consumaEnergia(trasferta);
    }

    // ---------- Forze ----------

    private Forze calcolaForze(Squadra squadra) {
        double[] somme = new double[3];
        double portiere = 0;
        boolean haPortiere = false;
        for (Posto posto : squadra.posti) {
            Giocatore giocatore = posto.giocatore;
            if (giocatore == null) {
                continue;
            }
            double fattore = condizione(giocatore) * ForzaSquadra.adattamento(posto.posizione, giocatore.dati.posizione());
            for (int zona = 0; zona < 3; zona++) {
                double peso = ForzaSquadra.peso(posto.posizione, zona);
                if (peso > 0) {
                    somme[zona] += peso * ForzaSquadra.contributo(zona, giocatore.dati.attributi()) * fattore;
                }
            }
            if (posto.posizione == Posizione.PORTIERE) {
                portiere = ForzaSquadra.forzaPortiere(giocatore.dati.attributi()) * fattore;
                haPortiere = true;
            }
        }
        Modulo modulo = squadra.dati.modulo();
        return new Forze(ForzaSquadra.indice(modulo, ForzaSquadra.ATTACCO, somme[ForzaSquadra.ATTACCO]),
                ForzaSquadra.indice(modulo, ForzaSquadra.CREAZIONE, somme[ForzaSquadra.CREAZIONE]),
                ForzaSquadra.indice(modulo, ForzaSquadra.DIFESA, somme[ForzaSquadra.DIFESA])
                        * moltiplicatoreDifesaMentalita(squadra.istruzioni.mentalita()),
                haPortiere ? ForzaSquadra.indicePortiere(portiere) : 0.2);
    }

    /** Quanto rende un giocatore in questo momento: stanchezza, forma e morale */
    private double condizione(Giocatore giocatore) {
        return (0.6 + 0.4 * giocatore.energia / 100.0) * (0.92 + 0.08 * giocatore.dati.forma() / 100.0)
                * (0.96 + 0.04 * giocatore.dati.morale() / 100.0);
    }

    // ---------- Effetto delle istruzioni ----------

    private static double moltiplicatoreRitmo(Ritmo ritmo) {
        return switch (ritmo) {
            case LENTO -> 0.90;
            case NORMALE -> 1.0;
            case VELOCE -> 1.10;
        };
    }

    private static double moltiplicatoreAttaccoMentalita(Mentalita mentalita) {
        return switch (mentalita) {
            case MOLTO_DIFENSIVA -> 0.85;
            case DIFENSIVA -> 0.93;
            case EQUILIBRATA -> 1.0;
            case OFFENSIVA -> 1.07;
            case MOLTO_OFFENSIVA -> 1.14;
        };
    }

    /** Una squadra molto offensiva lascia spazi: l'avversario costruisce più azioni */
    private static double moltiplicatoreConcessioneMentalita(Mentalita mentalita) {
        return switch (mentalita) {
            case MOLTO_DIFENSIVA -> 0.94;
            case DIFENSIVA -> 0.97;
            case EQUILIBRATA -> 1.0;
            case OFFENSIVA -> 1.04;
            case MOLTO_OFFENSIVA -> 1.08;
        };
    }

    /** Chi si copre protegge meglio la porta (e chi spinge è più scoperto) */
    private static double moltiplicatoreDifesaMentalita(Mentalita mentalita) {
        return switch (mentalita) {
            case MOLTO_DIFENSIVA -> 1.10;
            case DIFENSIVA -> 1.05;
            case EQUILIBRATA -> 1.0;
            case OFFENSIVA -> 0.96;
            case MOLTO_OFFENSIVA -> 0.92;
        };
    }

    /** Il pressing alto della squadra che difende riduce le azioni dell'avversario */
    private static double moltiplicatoreSequenzeDaPressing(Pressing pressing) {
        return switch (pressing) {
            case BASSO -> 1.04;
            case MEDIO -> 1.0;
            case ALTO -> 0.94;
        };
    }

    private static double moltiplicatoreSequenzeDaLinea(LineaDifensiva linea) {
        return switch (linea) {
            case BASSA -> 1.04;
            case MEDIA -> 1.0;
            case ALTA -> 0.96;
        };
    }

    /** Chi preme alto recupera palla più vicino alla porta avversaria: più azioni, ma più fatica */
    private static double moltiplicatoreSequenzeProprieDaPressing(Pressing pressing) {
        return switch (pressing) {
            case BASSO -> 0.97;
            case MEDIO -> 1.0;
            case ALTO -> 1.04;
        };
    }

    /** Una linea alta accorcia il campo e fa salire la squadra */
    private static double moltiplicatoreSequenzeProprieDaLinea(LineaDifensiva linea) {
        return switch (linea) {
            case BASSA -> 0.97;
            case MEDIA -> 1.0;
            case ALTA -> 1.03;
        };
    }

    /** Una linea alta lascia spazio alle spalle: le occasioni avversarie sono più pericolose */
    private static double moltiplicatoreQualitaDaLinea(LineaDifensiva linea) {
        return switch (linea) {
            case BASSA -> 0.95;
            case MEDIA -> 1.0;
            case ALTA -> 1.08;
        };
    }

    private static double moltiplicatoreConsumo(Pressing pressing, Ritmo ritmo) {
        double perPressing = switch (pressing) {
            case BASSO -> 0.88;
            case MEDIO -> 1.0;
            case ALTO -> 1.25;
        };
        double perRitmo = switch (ritmo) {
            case LENTO -> 0.90;
            case NORMALE -> 1.0;
            case VELOCE -> 1.15;
        };
        return perPressing * perRitmo;
    }

    private static double moltiplicatoreFalli(Pressing pressing) {
        return switch (pressing) {
            case BASSO -> 0.85;
            case MEDIO -> 1.0;
            case ALTO -> 1.20;
        };
    }

    // ---------- Azioni offensive ----------

    private void sequenza(Squadra attacco, Squadra difesa, double rapporto) {
        if (r.nextDouble() < PROBABILITA_RIGORE) {
            rigore(attacco, difesa);
            return;
        }
        double probabilitaTiro = limita(0.62 * Math.pow(rapporto, 0.7), 0.25, 0.92);
        if (r.nextDouble() < probabilitaTiro) {
            tiro(attacco, difesa, rapporto);
            return;
        }
        double sorteggio = r.nextDouble();
        if (sorteggio < 0.14) {
            Giocatore protagonista = scegliTiratore(attacco);
            if (protagonista != null) {
                evento(TipoEvento.OCCASIONE, attacco.lato, protagonista, null, null,
                        Cronaca.occasione(r, protagonista.dati.cognome(), attacco.dati.nome()), minuto, recupero);
            }
        } else if (sorteggio < 0.40) {
            calcioDAngolo(attacco);
        }
    }

    private void tiro(Squadra attacco, Squadra difesa, double rapporto) {
        Giocatore tiratore = scegliTiratore(attacco);
        if (tiratore == null) {
            return;
        }
        double xg = 0.024 + 0.32 * Math.pow(r.nextDouble(), 2.6);
        xg *= Math.pow(rapporto, 0.35) * moltiplicatoreQualitaDaLinea(difesa.istruzioni.lineaDifensiva());
        conclusione(attacco, difesa, tiratore, limita(xg, 0.01, 0.85), false);
    }

    private void rigore(Squadra attacco, Squadra difesa) {
        Giocatore tiratore = attacco.inCampo().stream()
                .filter(g -> g.posto != attacco.posti[0])
                .max(Comparator.comparingInt(g -> g.dati.attributi().tiro()))
                .orElse(null);
        if (tiratore == null) {
            return;
        }
        difesa.falli++;
        evento(TipoEvento.RIGORE, attacco.lato, null, null, null, Cronaca.rigoreAssegnato(r, attacco.dati.nome()),
                minuto, recupero);
        conclusione(attacco, difesa, tiratore, 0.76, true);
    }

    /** Dal tiro al risultato: gol, parata, deviazione, palo o fuori. xG è la probabilità media di segnare. */
    private void conclusione(Squadra attacco, Squadra difesa, Giocatore tiratore, double xg, boolean rigore) {
        attacco.tiri++;
        tiratore.tiri++;
        attacco.xg += xg;
        BigDecimal xgDecimale = BigDecimal.valueOf(xg).setScale(2, RoundingMode.HALF_UP);

        Giocatore portiere = portiereIn(difesa);
        double efficaciaTiro = 0.75 + 0.5 * tiratore.dati.attributi().tiro() / 100.0 * condizione(tiratore);
        double efficaciaPortiere = portiere == null
                ? 1.3
                : 0.75 + 0.5 * portiere.dati.attributi().parate() / 100.0 * condizione(portiere);
        double probabilitaGol = limita(xg * efficaciaTiro / efficaciaPortiere, 0.003, 0.92);
        String cognome = tiratore.dati.cognome();

        if (r.nextDouble() < probabilitaGol) {
            attacco.tiriInPorta++;
            tiratore.gol++;
            if (attacco.lato == Lato.CASA) {
                golCasa++;
            } else {
                golTrasferta++;
            }
            Giocatore assistente = rigore || r.nextDouble() >= 0.62 ? null : scegliAssistente(attacco, tiratore);
            if (assistente != null) {
                assistente.assist++;
            }
            String frase = Cronaca.gol(r, cognome, assistente == null ? null : assistente.dati.cognome(),
                    attacco.dati.nome(), rigore);
            evento(TipoEvento.GOL, attacco.lato, tiratore, assistente, xgDecimale,
                    frase + " (" + golCasa + "-" + golTrasferta + ")", minuto, recupero);
            return;
        }

        double sorteggio = r.nextDouble();
        if (rigore) {
            if (sorteggio < 0.65) {
                parata(attacco, difesa, tiratore, portiere, xgDecimale, false);
            } else if (sorteggio < 0.80) {
                evento(TipoEvento.PALO, attacco.lato, tiratore, null, xgDecimale, Cronaca.palo(r, cognome), minuto, recupero);
            } else {
                evento(TipoEvento.TIRO_FUORI, attacco.lato, tiratore, null, xgDecimale, Cronaca.tiroFuori(r, cognome),
                        minuto, recupero);
            }
            return;
        }
        double sogliaParata = 0.22 + 0.35 * xg;
        if (sorteggio < sogliaParata) {
            parata(attacco, difesa, tiratore, portiere, xgDecimale, true);
        } else if (sorteggio < sogliaParata + 0.20) {
            evento(TipoEvento.TIRO_BLOCCATO, attacco.lato, tiratore, null, xgDecimale,
                    Cronaca.tiroBloccato(r, cognome), minuto, recupero);
            if (r.nextDouble() < 0.45) {
                calcioDAngolo(attacco);
            }
        } else if (sorteggio < sogliaParata + 0.22) {
            evento(TipoEvento.PALO, attacco.lato, tiratore, null, xgDecimale, Cronaca.palo(r, cognome), minuto, recupero);
        } else {
            evento(TipoEvento.TIRO_FUORI, attacco.lato, tiratore, null, xgDecimale, Cronaca.tiroFuori(r, cognome),
                    minuto, recupero);
        }
    }

    private void parata(Squadra attacco, Squadra difesa, Giocatore tiratore, Giocatore portiere, BigDecimal xg,
            boolean possibileAngolo) {
        attacco.tiriInPorta++;
        if (portiere != null) {
            portiere.parate++;
        }
        evento(TipoEvento.TIRO_PARATO, attacco.lato, tiratore, portiere, xg,
                Cronaca.tiroParato(r, tiratore.dati.cognome(), portiere == null ? "il portiere" : portiere.dati.cognome()),
                minuto, recupero);
        if (possibileAngolo && r.nextDouble() < 0.45) {
            calcioDAngolo(attacco);
        }
    }

    private void calcioDAngolo(Squadra attacco) {
        attacco.angoli++;
        if (r.nextBoolean()) {
            evento(TipoEvento.CALCIO_D_ANGOLO, attacco.lato, null, null, null, Cronaca.angolo(r, attacco.dati.nome()),
                    minuto, recupero);
        }
    }

    private Giocatore scegliTiratore(Squadra squadra) {
        return scegliPesato(squadra.inCampo(), giocatore -> {
            if (giocatore.posto == squadra.posti[0]) {
                return 0; // chi sta in porta non tira
            }
            Posto posto = giocatore.posto;
            double zona = posto == null ? 0 : ForzaSquadra.peso(posto.posizione, ForzaSquadra.ATTACCO);
            return (0.03 + zona) * giocatore.dati.attributi().tiro() * condizione(giocatore);
        });
    }

    private Giocatore scegliAssistente(Squadra squadra, Giocatore tiratore) {
        return scegliPesato(squadra.inCampo(), giocatore -> {
            if (giocatore == tiratore || (giocatore.posto != null && giocatore.posto.posizione == Posizione.PORTIERE)) {
                return 0;
            }
            double zona = giocatore.posto == null ? 0 : ForzaSquadra.peso(giocatore.posto.posizione, ForzaSquadra.CREAZIONE);
            return (0.05 + zona) * giocatore.dati.attributi().passaggio() * condizione(giocatore);
        });
    }

    private Giocatore portiereIn(Squadra squadra) {
        return squadra.posti[0].giocatore;
    }

    // ---------- Falli, cartellini, infortuni ----------

    private void fallo(double quotaCasa) {
        Squadra casa = squadra(Lato.CASA);
        Squadra trasferta = squadra(Lato.TRASFERTA);
        double pesoCasa = (1 - quotaCasa + 0.3) * moltiplicatoreFalli(casa.istruzioni.pressing());
        double pesoTrasferta = (quotaCasa + 0.3) * moltiplicatoreFalli(trasferta.istruzioni.pressing());
        Squadra colpevole = r.nextDouble() < pesoCasa / (pesoCasa + pesoTrasferta) ? casa : trasferta;

        Giocatore giocatore = scegliPesato(colpevole.inCampo(), g -> {
            if (g.posto != null && g.posto.posizione == Posizione.PORTIERE) {
                return 0.1;
            }
            double zona = g.posto == null ? 0 : 0.2 + ForzaSquadra.peso(g.posto.posizione, ForzaSquadra.DIFESA);
            // Chi è già ammonito gioca con più prudenza
            double prudenza = g.ammonizioni > 0 ? 0.25 : 1.0;
            return zona * prudenza * (20 + g.dati.attributi().contrasto());
        });
        if (giocatore == null) {
            return;
        }
        colpevole.falli++;

        double sorteggio = r.nextDouble();
        if (sorteggio < 0.0025) {
            espelli(colpevole, giocatore, false);
        } else if (sorteggio < 0.0025 + 0.15) {
            ammonisci(colpevole, giocatore);
        } else if (r.nextDouble() < 0.10) {
            evento(TipoEvento.FALLO, colpevole.lato, giocatore, null, null, Cronaca.fallo(r, giocatore.dati.cognome()),
                    minuto, recupero);
        }
    }

    private void ammonisci(Squadra squadra, Giocatore giocatore) {
        giocatore.ammonizioni++;
        squadra.ammonizioni++;
        evento(TipoEvento.AMMONIZIONE, squadra.lato, giocatore, null, null,
                Cronaca.ammonizione(r, giocatore.dati.cognome(), squadra.dati.nome()), minuto, recupero);
        if (giocatore.ammonizioni >= 2) {
            espelli(squadra, giocatore, true);
        }
    }

    private void espelli(Squadra squadra, Giocatore giocatore, boolean doppiaAmmonizione) {
        giocatore.espulso = true;
        squadra.espulsioni++;
        evento(TipoEvento.ESPULSIONE, squadra.lato, giocatore, null, null,
                Cronaca.espulsione(r, giocatore.dati.cognome(), squadra.dati.nome(), doppiaAmmonizione), minuto, recupero);
        lasciaIlCampo(squadra, giocatore);
    }

    /** Un giocatore esce (espulso o infortunato senza cambio): il suo posto resta vuoto */
    private void lasciaIlCampo(Squadra squadra, Giocatore giocatore) {
        Posto posto = giocatore.posto;
        giocatore.inCampo = false;
        giocatore.minutoUscita = minuto;
        giocatore.posto = null;
        if (posto != null) {
            posto.giocatore = null;
            // Se esce il portiere e c'è un portiere in panchina con un cambio libero, entra al posto di un giocatore di movimento
            if (posto.posizione == Posizione.PORTIERE) {
                sistemaLaPorta(squadra, posto);
            }
        }
    }

    private void sistemaLaPorta(Squadra squadra, Posto portaVuota) {
        Optional<Giocatore> portiereDiRiserva = squadra.panchina().stream()
                .filter(g -> g.dati.posizione() == Posizione.PORTIERE).findFirst();
        List<Giocatore> movimento = squadra.inCampo();
        if (portiereDiRiserva.isPresent() && squadra.sostituzioni < MASSIMO_SOSTITUZIONI && !movimento.isEmpty()) {
            // Si sacrifica il giocatore di movimento che vale meno
            Giocatore sacrificato = movimento.stream().min(Comparator.comparingDouble(g -> g.valutazione * condizione(g))).get();
            Posto postoLibero = sacrificato.posto;
            eseguiSostituzione(squadra, sacrificato, portiereDiRiserva.get(), false);
            // Il portiere è entrato nel posto del sacrificato: lo si sposta in porta
            Giocatore nuovo = portiereDiRiserva.get();
            postoLibero.giocatore = null;
            nuovo.posto = portaVuota;
            portaVuota.giocatore = nuovo;
        } else if (!movimento.isEmpty()) {
            // Un giocatore di movimento va in porta: rende molto meno
            Giocatore volontario = movimento.stream()
                    .max(Comparator.comparingInt(g -> g.dati.attributi().parate() + g.dati.attributi().intelligenza()))
                    .get();
            Posto vecchio = volontario.posto;
            vecchio.giocatore = null;
            volontario.posto = portaVuota;
            portaVuota.giocatore = volontario;
        }
    }

    private void infortunio(Squadra squadra) {
        Giocatore giocatore = scegliPesato(squadra.inCampo(), g -> 20 + (100 - g.energia));
        if (giocatore == null) {
            return;
        }
        giocatore.infortunato = true;
        int giorni = Math.min(150, 3 + (int) (-Math.log(1 - r.nextDouble()) * 14));
        infortuni.add(new InfortunioSimulato(giocatore.dati.id(), squadra.lato, giorni, tipoDiInfortunio(giorni)));
        evento(TipoEvento.INFORTUNIO, squadra.lato, giocatore, null, null,
                Cronaca.infortunio(r, giocatore.dati.cognome(), squadra.dati.nome()), minuto, recupero);

        Giocatore sostituto = squadra.sostituzioni < MASSIMO_SOSTITUZIONI ? scegliSostituto(squadra, giocatore) : null;
        if (sostituto != null) {
            eseguiSostituzione(squadra, giocatore, sostituto, true);
        } else {
            lasciaIlCampo(squadra, giocatore);
        }
    }

    private static String tipoDiInfortunio(int giorni) {
        if (giorni <= 7) {
            return "Contusione";
        }
        if (giorni <= 21) {
            return "Distorsione alla caviglia";
        }
        if (giorni <= 45) {
            return "Stiramento muscolare";
        }
        if (giorni <= 90) {
            return "Strappo muscolare";
        }
        return "Lesione ai legamenti del ginocchio";
    }

    // ---------- Sostituzioni ----------

    private Giocatore scegliSostituto(Squadra squadra, Giocatore uscente) {
        Posizione posizione = uscente.posto != null ? uscente.posto.posizione : uscente.dati.posizione();
        Ruolo ruolo = posizione.getRuolo();
        List<Giocatore> disponibili = squadra.panchina();
        if (posizione == Posizione.PORTIERE) {
            return disponibili.stream().filter(g -> g.dati.posizione() == Posizione.PORTIERE).findFirst()
                    .orElse(null);
        }
        Optional<Giocatore> stessaPosizione = disponibili.stream().filter(g -> g.dati.posizione() == posizione)
                .max(Comparator.comparingInt(g -> g.valutazione));
        if (stessaPosizione.isPresent()) {
            return stessaPosizione.get();
        }
        Optional<Giocatore> stessoReparto = disponibili.stream()
                .filter(g -> g.dati.posizione() != Posizione.PORTIERE && g.dati.posizione().getRuolo() == ruolo)
                .max(Comparator.comparingInt(g -> g.valutazione));
        if (stessoReparto.isPresent()) {
            return stessoReparto.get();
        }
        return disponibili.stream().filter(g -> g.dati.posizione() != Posizione.PORTIERE)
                .max(Comparator.comparingInt(g -> g.valutazione)).orElse(null);
    }

    private void eseguiSostituzione(Squadra squadra, Giocatore esce, Giocatore entra, boolean perInfortunio) {
        Posto posto = esce.posto;
        int minutoDelCambio = inIntervallo ? 45 : minuto; // i cambi all'intervallo valgono 45 minuti al primo tempo
        esce.inCampo = false;
        esce.minutoUscita = minutoDelCambio;
        esce.posto = null;
        entra.inCampo = true;
        entra.entrato = true;
        entra.minutoIngresso = minutoDelCambio;
        entra.posto = posto;
        if (posto != null) {
            posto.giocatore = entra;
        }
        squadra.sostituzioni++;
        evento(TipoEvento.SOSTITUZIONE, squadra.lato, esce, entra, null,
                Cronaca.sostituzione(r, squadra.dati.nome(), esce.dati.cognome(), entra.dati.cognome(), perInfortunio),
                minuto, recupero);
    }

    /** Nel secondo tempo, due volte al massimo per controllo, esce il giocatore più stanco se in panchina c'è di meglio */
    private void sostituzioniPerStanchezza(Squadra squadra) {
        for (int i = 0; i < 2 && squadra.sostituzioni < MASSIMO_SOSTITUZIONI; i++) {
            Optional<Giocatore> stanco = squadra.inCampo().stream()
                    .filter(g -> g.posto != null && g.posto.posizione != Posizione.PORTIERE && g.energia < 55)
                    .min(Comparator.comparingDouble(g -> g.energia));
            if (stanco.isEmpty()) {
                return;
            }
            Giocatore sostituto = scegliSostituto(squadra, stanco.get());
            if (sostituto == null || sostituto.valutazione * 1.0 < stanco.get().valutazione * condizione(stanco.get()) * 0.98) {
                return;
            }
            eseguiSostituzione(squadra, stanco.get(), sostituto, false);
        }
    }

    /** La squadra del computer osa di più se perde e si copre se vince */
    private void adattaMentalita(Squadra squadra) {
        int differenza = gol(squadra.lato) - gol(squadra.lato.avversario());
        int spostamento = differenza <= -2 ? 2 : differenza == -1 ? 1 : differenza >= 2 ? -1 : 0;
        Mentalita[] livelli = Mentalita.values();
        int nuovo = Math.max(0, Math.min(livelli.length - 1, squadra.istruzioniIniziali.mentalita().ordinal() + spostamento));
        squadra.istruzioni = squadra.istruzioniIniziali.conMentalita(livelli[nuovo]);
    }

    // ---------- Energia ----------

    private void consumaEnergia(Squadra squadra) {
        double consumo = moltiplicatoreConsumo(squadra.istruzioni.pressing(), squadra.istruzioni.ritmo());
        for (Giocatore giocatore : squadra.inCampo()) {
            double resistenza = Math.max(30, giocatore.dati.attributi().resistenza());
            double perMinuto = 0.40 * consumo * (65.0 / resistenza);
            if (giocatore.posto != null && giocatore.posto.posizione == Posizione.PORTIERE) {
                perMinuto *= 0.4;
            }
            giocatore.energia = Math.max(0, giocatore.energia - perMinuto);
        }
    }

    // ---------- Strumenti ----------

    private void evento(TipoEvento tipo, Lato lato, Giocatore primo, Giocatore secondo, BigDecimal xg, String testo,
            int minutoEvento, int recuperoEvento) {
        ordine++;
        eventi.add(new EventoSimulato(ordine, minutoEvento, recuperoEvento, tipo, lato,
                primo == null ? null : primo.dati.id(), secondo == null ? null : secondo.dati.id(), xg, testo));
    }

    private interface Peso {
        double di(Giocatore giocatore);
    }

    private Giocatore scegliPesato(List<Giocatore> candidati, Peso peso) {
        double totale = 0;
        double[] pesi = new double[candidati.size()];
        for (int i = 0; i < pesi.length; i++) {
            pesi[i] = Math.max(0, peso.di(candidati.get(i)));
            totale += pesi[i];
        }
        if (totale <= 0) {
            return null;
        }
        double sorteggio = r.nextDouble() * totale;
        for (int i = 0; i < pesi.length; i++) {
            sorteggio -= pesi[i];
            if (sorteggio < 0) {
                return candidati.get(i);
            }
        }
        return candidati.get(candidati.size() - 1);
    }

    private static double limita(double valore, double minimo, double massimo) {
        return Math.max(minimo, Math.min(massimo, valore));
    }

    // ---------- Risultati ----------

    private StatisticheSquadra statistiche(Squadra squadra) {
        int possesso = squadra.minutiControllo == 0 ? 50 : (int) Math.round(100.0 * squadra.controllo / squadra.minutiControllo);
        return new StatisticheSquadra(possesso, squadra.tiri, squadra.tiriInPorta,
                Math.round(squadra.xg * 100) / 100.0, squadra.falli, squadra.angoli, squadra.ammonizioni,
                squadra.espulsioni);
    }

    RisultatoPrimoTempo risultatoPrimoTempo() {
        List<SituazioneGiocatore> situazione = new ArrayList<>();
        for (Squadra squadra : squadre) {
            for (Giocatore g : squadra.tutti) {
                situazione.add(new SituazioneGiocatore(g.dati.id(), squadra.lato, g.inCampo, g.espulso,
                        g.ammonizioni > 0, g.infortunato, (int) Math.round(g.energia), g.gol, g.assist, g.tiri));
            }
        }
        return new RisultatoPrimoTempo(golCasa, golTrasferta, List.copyOf(eventi), statistiche(squadra(Lato.CASA)),
                statistiche(squadra(Lato.TRASFERTA)), situazione);
    }

    RisultatoPartita risultatoFinale() {
        List<PrestazioneSimulata> prestazioni = new ArrayList<>();
        for (Squadra squadra : squadre) {
            int golFatti = gol(squadra.lato);
            int golSubiti = gol(squadra.lato.avversario());
            for (Giocatore g : squadra.tutti) {
                if (!g.entrato) {
                    continue;
                }
                int uscita = g.minutoUscita >= 0 ? g.minutoUscita : 90;
                int minuti = Math.max(0, Math.min(90, uscita) - g.minutoIngresso);
                prestazioni.add(new PrestazioneSimulata(g.dati.id(), squadra.lato, g.titolare, minuti, g.gol, g.assist,
                        g.tiri, g.ammonizioni > 0, g.espulso, voto(g, golFatti, golSubiti, minuti),
                        (int) Math.round(g.energia)));
            }
        }
        return new RisultatoPartita(golCasa, golTrasferta, List.copyOf(eventi), statistiche(squadra(Lato.CASA)),
                statistiche(squadra(Lato.TRASFERTA)), prestazioni, List.copyOf(infortuni));
    }

    /** Il voto parte da 6: contano gol, assist, parate, il risultato e i cartellini */
    private double voto(Giocatore g, int golFatti, int golSubiti, int minuti) {
        double voto = 6.0 + g.gol * 0.9 + g.assist * 0.5;
        voto += golFatti > golSubiti ? 0.25 : golFatti < golSubiti ? -0.25 : 0;
        Posizione posizione = g.dati.posizione();
        if (posizione == Posizione.PORTIERE) {
            voto += g.parate * 0.18 - golSubiti * 0.12;
        } else if (posizione.getRuolo() == Ruolo.DIFENSORE) {
            voto += golSubiti == 0 ? 0.3 : -0.08 * golSubiti;
        }
        voto -= g.ammonizioni > 0 ? 0.3 : 0;
        voto -= g.espulso ? 1.5 : 0;
        voto += r.nextGaussian() * 0.35;
        if (minuti < 45) {
            voto = 6.0 + (voto - 6.0) * minuti / 45.0;
        }
        return Math.round(limita(voto, 3.0, 10.0) * 10) / 10.0;
    }
}
