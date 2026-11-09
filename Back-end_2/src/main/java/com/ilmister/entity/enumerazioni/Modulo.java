package com.ilmister.entity.enumerazioni;

import static com.ilmister.entity.enumerazioni.Posizione.ALA_DESTRA;
import static com.ilmister.entity.enumerazioni.Posizione.ALA_SINISTRA;
import static com.ilmister.entity.enumerazioni.Posizione.ATTACCANTE;
import static com.ilmister.entity.enumerazioni.Posizione.CENTROCAMPISTA;
import static com.ilmister.entity.enumerazioni.Posizione.DIFENSORE_CENTRALE;
import static com.ilmister.entity.enumerazioni.Posizione.ESTERNO_DESTRO;
import static com.ilmister.entity.enumerazioni.Posizione.ESTERNO_SINISTRO;
import static com.ilmister.entity.enumerazioni.Posizione.MEDIANO;
import static com.ilmister.entity.enumerazioni.Posizione.PORTIERE;
import static com.ilmister.entity.enumerazioni.Posizione.TERZINO_DESTRO;
import static com.ilmister.entity.enumerazioni.Posizione.TERZINO_SINISTRO;
import static com.ilmister.entity.enumerazioni.Posizione.TREQUARTISTA;

import java.util.List;

/**
 * Il modulo di gioco: i suoi 11 posti, nell'ordine in cui si scrivono in formazione
 * (il posto 0 è sempre il portiere).
 */
public enum Modulo {
    M_4_3_3("4-3-3", List.of(PORTIERE, TERZINO_DESTRO, DIFENSORE_CENTRALE, DIFENSORE_CENTRALE, TERZINO_SINISTRO,
            MEDIANO, CENTROCAMPISTA, CENTROCAMPISTA, ALA_DESTRA, ATTACCANTE, ALA_SINISTRA)),
    M_4_4_2("4-4-2", List.of(PORTIERE, TERZINO_DESTRO, DIFENSORE_CENTRALE, DIFENSORE_CENTRALE, TERZINO_SINISTRO,
            ESTERNO_DESTRO, CENTROCAMPISTA, CENTROCAMPISTA, ESTERNO_SINISTRO, ATTACCANTE, ATTACCANTE)),
    M_4_2_3_1("4-2-3-1", List.of(PORTIERE, TERZINO_DESTRO, DIFENSORE_CENTRALE, DIFENSORE_CENTRALE, TERZINO_SINISTRO,
            MEDIANO, MEDIANO, ALA_DESTRA, TREQUARTISTA, ALA_SINISTRA, ATTACCANTE)),
    M_3_5_2("3-5-2", List.of(PORTIERE, DIFENSORE_CENTRALE, DIFENSORE_CENTRALE, DIFENSORE_CENTRALE, ESTERNO_DESTRO,
            CENTROCAMPISTA, MEDIANO, CENTROCAMPISTA, ESTERNO_SINISTRO, ATTACCANTE, ATTACCANTE)),
    M_3_4_3("3-4-3", List.of(PORTIERE, DIFENSORE_CENTRALE, DIFENSORE_CENTRALE, DIFENSORE_CENTRALE, ESTERNO_DESTRO,
            CENTROCAMPISTA, CENTROCAMPISTA, ESTERNO_SINISTRO, ALA_DESTRA, ATTACCANTE, ALA_SINISTRA)),
    M_5_3_2("5-3-2", List.of(PORTIERE, TERZINO_DESTRO, DIFENSORE_CENTRALE, DIFENSORE_CENTRALE, DIFENSORE_CENTRALE,
            TERZINO_SINISTRO, CENTROCAMPISTA, MEDIANO, CENTROCAMPISTA, ATTACCANTE, ATTACCANTE));

    public static final int TITOLARI = 11;

    private final String codice;
    private final List<Posizione> posti;

    Modulo(String codice, List<Posizione> posti) {
        this.codice = codice;
        this.posti = posti;
    }

    /** Il nome del modulo come lo scrive un allenatore, ad esempio "4-3-3" */
    public String getCodice() {
        return codice;
    }

    public List<Posizione> getPosti() {
        return posti;
    }

    /** Quanti posti del modulo sono occupati da giocatori del reparto indicato */
    public long numeroPosti(Ruolo ruolo) {
        return posti.stream().filter(posizione -> posizione.getRuolo() == ruolo).count();
    }
}
