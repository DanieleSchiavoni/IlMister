package com.ilmister.entity.enumerazioni;

/** La posizione precisa di un calciatore; il reparto (ruolo) si ricava da qui */
public enum Posizione {
    PORTIERE(Ruolo.PORTIERE, "Portiere", "POR"),
    TERZINO_DESTRO(Ruolo.DIFENSORE, "Terzino destro", "TD"),
    TERZINO_SINISTRO(Ruolo.DIFENSORE, "Terzino sinistro", "TS"),
    DIFENSORE_CENTRALE(Ruolo.DIFENSORE, "Difensore centrale", "DC"),
    MEDIANO(Ruolo.CENTROCAMPISTA, "Mediano", "MED"),
    CENTROCAMPISTA(Ruolo.CENTROCAMPISTA, "Centrocampista", "CEN"),
    TREQUARTISTA(Ruolo.CENTROCAMPISTA, "Trequartista", "TRQ"),
    ESTERNO_DESTRO(Ruolo.CENTROCAMPISTA, "Esterno destro", "ED"),
    ESTERNO_SINISTRO(Ruolo.CENTROCAMPISTA, "Esterno sinistro", "ES"),
    ALA_DESTRA(Ruolo.ATTACCANTE, "Ala destra", "AD"),
    ALA_SINISTRA(Ruolo.ATTACCANTE, "Ala sinistra", "AS"),
    ATTACCANTE(Ruolo.ATTACCANTE, "Attaccante", "ATT");

    private final Ruolo ruolo;
    private final String descrizione;
    private final String sigla;

    Posizione(Ruolo ruolo, String descrizione, String sigla) {
        this.ruolo = ruolo;
        this.descrizione = descrizione;
        this.sigla = sigla;
    }

    public Ruolo getRuolo() {
        return ruolo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public String getSigla() {
        return sigla;
    }
}
