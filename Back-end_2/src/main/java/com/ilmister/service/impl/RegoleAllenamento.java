package com.ilmister.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.SplittableRandom;

import com.ilmister.entity.enumerazioni.FocusAllenamento;
import com.ilmister.entity.enumerazioni.Posizione;
import com.ilmister.generazione.Attributo;
import com.ilmister.generazione.CalcolatoreAttributi;

/** Come si allenano i giocatori: funzioni pure, che lavorano sul vettore degli attributi */
final class RegoleAllenamento {

    /** Un miglioramento di un punto su un attributo, con la nuova valutazione complessiva */
    record Progresso(Attributo attributo, int nuovoValore, int nuovaValutazione) {
    }

    private RegoleAllenamento() {
    }

    /** La probabilità che un giocatore migliori in una giornata: i giovani crescono, i vecchi quasi no */
    static double probabilita(int eta) {
        if (eta <= 20) {
            return 0.40;
        }
        if (eta <= 23) {
            return 0.30;
        }
        if (eta <= 27) {
            return 0.18;
        }
        if (eta <= 31) {
            return 0.08;
        }
        return 0.02;
    }

    /** Gli attributi su cui un focus può far crescere un giocatore di quel ruolo */
    static List<Attributo> candidati(FocusAllenamento focus, Posizione posizione) {
        List<Attributo> scelti = new ArrayList<>(switch (focus) {
            case FISICO -> List.of(Attributo.VELOCITA, Attributo.RESISTENZA, Attributo.FORZA);
            case TECNICO -> List.of(Attributo.TECNICA, Attributo.PASSAGGIO, Attributo.DRIBBLING);
            case DIFENSIVO -> List.of(Attributo.CONTRASTO, Attributo.INTELLIGENZA, Attributo.FORZA, Attributo.PARATE);
            case OFFENSIVO -> List.of(Attributo.TIRO, Attributo.DRIBBLING, Attributo.VELOCITA);
            case EQUILIBRATO -> List.of(Attributo.values());
        });
        // Conta solo ciò che serve davvero nel ruolo (un portiere non migliora nel tiro, un attaccante nelle parate)
        scelti.removeIf(attributo -> CalcolatoreAttributi.peso(posizione, attributo) <= 0);
        return scelti;
    }

    /**
     * Prova a far migliorare un giocatore di un punto. Non succede se ha già raggiunto il potenziale.
     * La nuova valutazione cresce quanto la valutazione calcolata dagli attributi, senza superare il potenziale.
     */
    static Optional<Progresso> prova(FocusAllenamento focus, Posizione posizione, int eta, int[] attributi,
            int valutazione, int potenziale, long seme) {
        if (valutazione >= potenziale) {
            return Optional.empty();
        }
        SplittableRandom casuale = new SplittableRandom(seme);
        if (casuale.nextDouble() >= probabilita(eta)) {
            return Optional.empty();
        }
        List<Attributo> possibili = candidati(focus, posizione).stream()
                .filter(attributo -> attributi[attributo.ordinal()] < CalcolatoreAttributi.MASSIMO)
                .toList();
        if (possibili.isEmpty()) {
            return Optional.empty();
        }
        Attributo scelto = possibili.get(casuale.nextInt(possibili.size()));

        int prima = CalcolatoreAttributi.valutazione(posizione, com.ilmister.generazione.Attributi.daVettore(attributi));
        int[] dopoVettore = attributi.clone();
        dopoVettore[scelto.ordinal()]++;
        int dopo = CalcolatoreAttributi.valutazione(posizione, com.ilmister.generazione.Attributi.daVettore(dopoVettore));

        int nuovaValutazione = Math.min(potenziale, Math.min(CalcolatoreAttributi.MASSIMO, valutazione + (dopo - prima)));
        return Optional.of(new Progresso(scelto, dopoVettore[scelto.ordinal()], nuovaValutazione));
    }
}
