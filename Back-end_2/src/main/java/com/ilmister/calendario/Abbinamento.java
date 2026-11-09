package com.ilmister.calendario;

/** Una partita del calendario: chi gioca in casa e chi in trasferta. */
public record Abbinamento(long casaId, long trasfertaId) {

    public Abbinamento invertito() {
        return new Abbinamento(trasfertaId, casaId);
    }
}
