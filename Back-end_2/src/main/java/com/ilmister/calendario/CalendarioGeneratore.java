package com.ilmister.calendario;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Genera il calendario di un girone all'italiana (andata e ritorno) con il metodo del cerchio.
 * Il seme rende il calendario ripetibile.
 */
public final class CalendarioGeneratore {

    private CalendarioGeneratore() {
    }

    /** Restituisce, per ogni giornata, la lista degli abbinamenti. Servono almeno 2 squadre, in numero pari. */
    public static List<List<Abbinamento>> genera(List<Long> squadre, long seme) {
        int n = squadre.size();
        if (n < 2 || n % 2 != 0) {
            throw new IllegalArgumentException("Servono almeno 2 squadre e in numero pari");
        }
        List<Long> ordine = new ArrayList<>(squadre);
        Collections.shuffle(ordine, new Random(seme));

        List<List<Abbinamento>> andata = new ArrayList<>();
        for (int giornata = 0; giornata < n - 1; giornata++) {
            List<Abbinamento> partite = new ArrayList<>();
            // Metodo del cerchio canonico: la squadra n-1 e' ferma, le altre n-1 ruotano (indici 0..n-2).
            int m = n - 1;
            Long ferma = ordine.get(m);
            Long primaFerma = ordine.get(giornata);
            partite.add(giornata % 2 == 0 ? new Abbinamento(ferma, primaFerma) : new Abbinamento(primaFerma, ferma));
            for (int k = 1; k <= n / 2 - 1; k++) {
                Long x = ordine.get(((giornata + k) % m + m) % m);
                Long y = ordine.get(((giornata - k) % m + m) % m);
                partite.add(k % 2 == 1 ? new Abbinamento(x, y) : new Abbinamento(y, x));
            }
            andata.add(partite);
        }

        List<List<Abbinamento>> calendario = new ArrayList<>(andata);
        for (List<Abbinamento> giornata : andata) {
            List<Abbinamento> ritorno = new ArrayList<>();
            for (Abbinamento a : giornata) {
                ritorno.add(a.invertito());
            }
            calendario.add(ritorno);
        }
        return calendario;
    }

    /** Date delle giornate: una domenica a settimana dal 13 settembre, con pausa dal 22/12 al 6/01. */
    public static List<LocalDate> date(int annoInizio, int numeroGiornate) {
        LocalDate data = LocalDate.of(annoInizio, 9, 13);
        while (data.getDayOfWeek() != DayOfWeek.SUNDAY) {
            data = data.plusDays(1);
        }
        LocalDate pausaDa = LocalDate.of(annoInizio, 12, 22);
        LocalDate pausaA = LocalDate.of(annoInizio + 1, 1, 6);
        List<LocalDate> date = new ArrayList<>();
        while (date.size() < numeroGiornate) {
            if (data.isBefore(pausaDa) || data.isAfter(pausaA)) {
                date.add(data);
            }
            data = data.plusWeeks(1);
        }
        return date;
    }
}
