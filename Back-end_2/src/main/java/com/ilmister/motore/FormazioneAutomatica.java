package com.ilmister.motore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import com.ilmister.entity.enumerazioni.Modulo;
import com.ilmister.entity.enumerazioni.Posizione;
import com.ilmister.entity.enumerazioni.Ruolo;
import com.ilmister.generazione.CalcolatoreAttributi;

/**
 * Sceglie da sola una formazione: per ogni posto del modulo il miglior giocatore disponibile in quella posizione
 * (o, mancando, dello stesso reparto), poi una panchina equilibrata. Serve alle squadre del computer e
 * come proposta iniziale per l'allenatore.
 */
public final class FormazioneAutomatica {

    public static final int GIOCATORI_IN_PANCHINA = 7;

    /** I titolari (nell'ordine dei posti del modulo) e la panchina */
    public record Scelta(List<GiocatoreInCampo> titolari, List<GiocatoreInCampo> panchina) {
    }

    private FormazioneAutomatica() {
    }

    public static Scelta scegli(List<GiocatoreInCampo> disponibili, Modulo modulo) {
        if (disponibili.size() < Modulo.TITOLARI) {
            throw new IllegalArgumentException("Servono almeno " + Modulo.TITOLARI + " giocatori disponibili");
        }
        List<GiocatoreInCampo> liberi = new ArrayList<>(disponibili);
        GiocatoreInCampo[] titolari = new GiocatoreInCampo[Modulo.TITOLARI];

        // Prima i posti più difficili da coprire: il portiere, poi gli altri nell'ordine del modulo
        for (int i = 0; i < titolari.length; i++) {
            Posizione posto = modulo.getPosti().get(i);
            GiocatoreInCampo scelto = migliore(liberi, g -> g.posizione() == posto)
                    .or(() -> migliore(liberi, g -> posto != Posizione.PORTIERE && g.posizione() != Posizione.PORTIERE
                            && g.posizione().getRuolo() == posto.getRuolo()))
                    .or(() -> migliore(liberi, g -> posto == Posizione.PORTIERE
                            ? g.posizione() == Posizione.PORTIERE : g.posizione() != Posizione.PORTIERE))
                    .or(() -> migliore(liberi, g -> true))
                    .orElseThrow();
            titolari[i] = scelto;
            liberi.remove(scelto);
        }

        List<GiocatoreInCampo> panchina = new ArrayList<>();
        aggiungiMigliori(panchina, liberi, g -> g.posizione() == Posizione.PORTIERE, 1);
        aggiungiMigliori(panchina, liberi, g -> g.posizione().getRuolo() == Ruolo.DIFENSORE, 2);
        aggiungiMigliori(panchina, liberi, g -> g.posizione().getRuolo() == Ruolo.CENTROCAMPISTA, 2);
        aggiungiMigliori(panchina, liberi, g -> g.posizione().getRuolo() == Ruolo.ATTACCANTE, 2);
        aggiungiMigliori(panchina, liberi, g -> true, GIOCATORI_IN_PANCHINA - panchina.size());
        return new Scelta(List.of(titolari), List.copyOf(panchina));
    }

    private static void aggiungiMigliori(List<GiocatoreInCampo> panchina, List<GiocatoreInCampo> liberi,
            Predicate<GiocatoreInCampo> filtro, int quanti) {
        for (int i = 0; i < quanti && panchina.size() < GIOCATORI_IN_PANCHINA; i++) {
            var scelto = migliore(liberi, filtro);
            if (scelto.isEmpty()) {
                return;
            }
            panchina.add(scelto.get());
            liberi.remove(scelto.get());
        }
    }

    private static java.util.Optional<GiocatoreInCampo> migliore(List<GiocatoreInCampo> candidati,
            Predicate<GiocatoreInCampo> filtro) {
        return candidati.stream().filter(filtro).max(Comparator.comparingDouble(FormazioneAutomatica::rendimento));
    }

    /** La valutazione del giocatore, ridotta se è stanco o in cattiva forma */
    private static double rendimento(GiocatoreInCampo giocatore) {
        int valutazione = CalcolatoreAttributi.valutazione(giocatore.posizione(), giocatore.attributi());
        return valutazione * (0.6 + 0.4 * giocatore.energia() / 100.0) * (0.92 + 0.08 * giocatore.forma() / 100.0);
    }
}
