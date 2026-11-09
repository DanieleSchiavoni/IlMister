package com.ilmister.generazione;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SplittableRandom;
import java.util.random.RandomGenerator;

import org.springframework.stereotype.Component;

import com.ilmister.entity.enumerazioni.Piede;
import com.ilmister.entity.enumerazioni.Posizione;

/**
 * Inventa un campionato: squadre con nome, stadio e colori, e per ognuna una rosa di 26 giocatori.
 * È ripetibile: con lo stesso seme e gli stessi parametri esce sempre lo stesso campionato.
 */
@Component
public class GeneratoreCampionato {

    /** La composizione di una rosa: abbastanza giocatori per ogni posizione di ogni modulo */
    static final Map<Posizione, Integer> COMPOSIZIONE_ROSA = new LinkedHashMap<>();

    static {
        COMPOSIZIONE_ROSA.put(Posizione.PORTIERE, 3);
        COMPOSIZIONE_ROSA.put(Posizione.TERZINO_DESTRO, 2);
        COMPOSIZIONE_ROSA.put(Posizione.TERZINO_SINISTRO, 2);
        COMPOSIZIONE_ROSA.put(Posizione.DIFENSORE_CENTRALE, 4);
        COMPOSIZIONE_ROSA.put(Posizione.MEDIANO, 2);
        COMPOSIZIONE_ROSA.put(Posizione.CENTROCAMPISTA, 3);
        COMPOSIZIONE_ROSA.put(Posizione.TREQUARTISTA, 2);
        COMPOSIZIONE_ROSA.put(Posizione.ESTERNO_DESTRO, 1);
        COMPOSIZIONE_ROSA.put(Posizione.ESTERNO_SINISTRO, 1);
        COMPOSIZIONE_ROSA.put(Posizione.ALA_DESTRA, 1);
        COMPOSIZIONE_ROSA.put(Posizione.ALA_SINISTRA, 1);
        COMPOSIZIONE_ROSA.put(Posizione.ATTACCANTE, 4);
    }

    /** Di quanto il giocatore è meglio o peggio della squadra: il primo di ogni posizione è il titolare */
    private static final int[] SCARTO_PER_ORDINE = { 3, -4, -8, -10 };

    /** I numeri di maglia preferiti per ogni posizione */
    private static final Map<Posizione, int[]> NUMERI_PREFERITI = new EnumMap<>(Posizione.class);

    static {
        NUMERI_PREFERITI.put(Posizione.PORTIERE, new int[] { 1, 12, 22 });
        NUMERI_PREFERITI.put(Posizione.TERZINO_DESTRO, new int[] { 2, 13 });
        NUMERI_PREFERITI.put(Posizione.TERZINO_SINISTRO, new int[] { 3, 15 });
        NUMERI_PREFERITI.put(Posizione.DIFENSORE_CENTRALE, new int[] { 4, 5, 14, 23 });
        NUMERI_PREFERITI.put(Posizione.MEDIANO, new int[] { 6, 16 });
        NUMERI_PREFERITI.put(Posizione.CENTROCAMPISTA, new int[] { 8, 20, 18 });
        NUMERI_PREFERITI.put(Posizione.TREQUARTISTA, new int[] { 10, 21 });
        NUMERI_PREFERITI.put(Posizione.ESTERNO_DESTRO, new int[] { 7, 17 });
        NUMERI_PREFERITI.put(Posizione.ESTERNO_SINISTRO, new int[] { 11, 19 });
        NUMERI_PREFERITI.put(Posizione.ALA_DESTRA, new int[] { 7, 25 });
        NUMERI_PREFERITI.put(Posizione.ALA_SINISTRA, new int[] { 11, 24 });
        NUMERI_PREFERITI.put(Posizione.ATTACCANTE, new int[] { 9, 19, 29, 30 });
    }

    public CampionatoGenerato genera(long seme, ConfigurazioneGenerazione configurazione, LocalDate inizioStagione) {
        SplittableRandom casuale = new SplittableRandom(seme);
        int numero = configurazione.numeroSquadre();

        List<String> citta = new ArrayList<>(NomiItaliani.CITTA);
        Collections.shuffle(citta, casuale);
        List<Integer> reputazioni = reputazioni(numero, casuale);
        List<String[]> colori = new ArrayList<>(NomiItaliani.COLORI);
        Collections.shuffle(colori, casuale);

        List<SquadraGenerata> squadre = new ArrayList<>();
        for (int i = 0; i < numero; i++) {
            String cittaSquadra = citta.get(i);
            String prefisso = NomiItaliani.PREFISSI.get(casuale.nextInt(NomiItaliani.PREFISSI.size()));
            String tipoStadio = NomiItaliani.TIPI_STADIO.get(casuale.nextInt(NomiItaliani.TIPI_STADIO.size()));
            int reputazione = reputazioni.get(i);
            String[] coloriSquadra = colori.get(i % colori.size());

            squadre.add(new SquadraGenerata(
                    prefisso + " " + cittaSquadra,
                    cittaSquadra,
                    cittaSquadra,
                    tipoStadio + " " + cittaSquadra,
                    800 + reputazione * 60,
                    reputazione,
                    coloriSquadra[0],
                    coloriSquadra[1],
                    BigDecimal.valueOf(Math.round(reputazione * 3500.0 / 1000) * 1000L),
                    generaRosa(reputazione, configurazione, inizioStagione, casuale)));
        }
        return new CampionatoGenerato(configurazione.nomeCampionato(), 1, seme, squadre);
    }

    /** Reputazioni distribuite da 40 a 80, con un po' di variazione, assegnate alle squadre a caso */
    private List<Integer> reputazioni(int numero, RandomGenerator casuale) {
        List<Integer> valori = new ArrayList<>();
        for (int i = 0; i < numero; i++) {
            int base = 40 + (int) Math.round(40.0 * i / (numero - 1));
            valori.add(Math.max(35, Math.min(85, base + casuale.nextInt(7) - 3)));
        }
        Collections.shuffle(valori, casuale);
        return valori;
    }

    private List<GiocatoreGenerato> generaRosa(int reputazione, ConfigurazioneGenerazione configurazione,
            LocalDate inizioStagione, RandomGenerator casuale) {
        double forzaSquadra = configurazione.livelloMedio() + (reputazione - 60) * 0.35;
        Set<String> cognomiUsati = new HashSet<>();
        Set<Integer> numeriUsati = new HashSet<>();
        List<GiocatoreGenerato> rosa = new ArrayList<>();

        for (Map.Entry<Posizione, Integer> voce : COMPOSIZIONE_ROSA.entrySet()) {
            Posizione posizione = voce.getKey();
            for (int ordine = 0; ordine < voce.getValue(); ordine++) {
                int scarto = SCARTO_PER_ORDINE[Math.min(ordine, SCARTO_PER_ORDINE.length - 1)];
                int valutazioneObiettivo = limita((int) Math.round(forzaSquadra + scarto + casuale.nextGaussian() * 3.5), 25, 90);
                rosa.add(generaGiocatore(posizione, valutazioneObiettivo, inizioStagione, cognomiUsati, numeriUsati, casuale));
            }
        }
        return rosa;
    }

    private GiocatoreGenerato generaGiocatore(Posizione posizione, int valutazioneObiettivo, LocalDate inizioStagione,
            Set<String> cognomiUsati, Set<Integer> numeriUsati, RandomGenerator casuale) {
        String nome = NomiItaliani.NOMI.get(casuale.nextInt(NomiItaliani.NOMI.size()));
        String cognome;
        do {
            cognome = NomiItaliani.COGNOMI.get(casuale.nextInt(NomiItaliani.COGNOMI.size()));
        } while (!cognomiUsati.add(cognome)); // la cronaca cita solo il cognome: in una squadra non ci sono due uguali

        int eta = scegliEta(posizione, casuale);
        LocalDate dataNascita = inizioStagione.minusYears(eta).minusDays(casuale.nextInt(365));

        Attributi attributi = CalcolatoreAttributi.genera(posizione, valutazioneObiettivo, casuale);
        int valutazione = CalcolatoreAttributi.valutazione(posizione, attributi);
        int potenziale = Math.min(CalcolatoreAttributi.MASSIMO, valutazione + margineDiCrescita(eta, casuale));

        BigDecimal valore = valoreDiMercato(valutazione, potenziale, eta);
        BigDecimal stipendio = BigDecimal.valueOf(Math.max(1000, Math.round(valore.doubleValue() * 0.18 / 100) * 100));

        return new GiocatoreGenerato(nome, cognome, dataNascita, posizione, scegliPiede(posizione, casuale),
                assegnaNumero(posizione, numeriUsati), attributi, valutazione, potenziale, stipendio,
                LocalDate.of(inizioStagione.getYear() + 1 + casuale.nextInt(3), 6, 30), valore);
    }

    /** L'età di un giocatore: più giovane o più matura a seconda della posizione (i portieri durano di più) */
    static int scegliEta(Posizione posizione, RandomGenerator casuale) {
        int sorteggio = casuale.nextInt(100);
        int eta;
        if (sorteggio < 15) {
            eta = 17 + casuale.nextInt(4);      // 17-20
        } else if (sorteggio < 50) {
            eta = 21 + casuale.nextInt(5);      // 21-25
        } else if (sorteggio < 80) {
            eta = 26 + casuale.nextInt(5);      // 26-30
        } else if (sorteggio < 97) {
            eta = 31 + casuale.nextInt(5);      // 31-35
        } else {
            eta = 36 + casuale.nextInt(3);      // 36-38
        }
        if (posizione == Posizione.PORTIERE && eta < 20) {
            eta += 3;
        }
        return Math.min(eta, 38);
    }

    /** Quanto può ancora crescere: molto da giovane, niente dopo i trent'anni */
    private int margineDiCrescita(int eta, RandomGenerator casuale) {
        if (eta <= 21) {
            return 6 + casuale.nextInt(13);
        }
        if (eta <= 25) {
            return 2 + casuale.nextInt(9);
        }
        if (eta <= 29) {
            return casuale.nextInt(5);
        }
        return 0;
    }

    /** Il valore sale del 12% per ogni punto di valutazione; i giovani con potenziale valgono di più, gli anziani meno */
    static BigDecimal valoreDiMercato(int valutazione, int potenziale, int eta) {
        double livello = (valutazione + potenziale) / 2.0;
        double valore = 3000 * Math.pow(1.12, livello - 40);
        if (eta >= 33) {
            valore *= 0.5;
        } else if (eta >= 30) {
            valore *= 0.75;
        }
        return BigDecimal.valueOf(Math.max(500, Math.round(valore / 100) * 100));
    }

    private Piede scegliPiede(Posizione posizione, RandomGenerator casuale) {
        int sorteggio = casuale.nextInt(100);
        return switch (posizione) {
            case TERZINO_SINISTRO, ESTERNO_SINISTRO, ALA_SINISTRA -> sorteggio < 75 ? Piede.SINISTRO
                    : sorteggio < 90 ? Piede.AMBIDESTRO : Piede.DESTRO;
            case TERZINO_DESTRO, ESTERNO_DESTRO, ALA_DESTRA -> sorteggio < 75 ? Piede.DESTRO
                    : sorteggio < 90 ? Piede.AMBIDESTRO : Piede.SINISTRO;
            default -> sorteggio < 75 ? Piede.DESTRO : sorteggio < 93 ? Piede.SINISTRO : Piede.AMBIDESTRO;
        };
    }

    /** Il primo numero libero tra quelli tipici della posizione; se sono tutti presi, il primo libero da 2 in su */
    private int assegnaNumero(Posizione posizione, Set<Integer> usati) {
        for (int numero : NUMERI_PREFERITI.get(posizione)) {
            if (usati.add(numero)) {
                return numero;
            }
        }
        int numero = 2;
        while (!usati.add(numero)) {
            numero++;
        }
        return numero;
    }

    private static int limita(int valore, int minimo, int massimo) {
        return Math.max(minimo, Math.min(massimo, valore));
    }
}
