package com.ilmister.calendario;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CalendarioGeneratoreTest {

    private static List<Long> squadre(int n) {
        List<Long> lista = new ArrayList<>();
        for (long i = 1; i <= n; i++) {
            lista.add(i);
        }
        return lista;
    }

    @Test
    void ognunaGiocaControTuttePerAndataERitorno() {
        var calendario = CalendarioGeneratore.genera(squadre(10), 42);
        assertThat(calendario).hasSize(18);
        Set<String> viste = new HashSet<>();
        for (var giornata : calendario) {
            assertThat(giornata).hasSize(5);
            Set<Long> presenti = new HashSet<>();
            for (var a : giornata) {
                presenti.add(a.casaId());
                presenti.add(a.trasfertaId());
                viste.add(a.casaId() + ">" + a.trasfertaId());
            }
            assertThat(presenti).hasSize(10);
        }
        assertThat(viste).hasSize(90);
    }

    @Test
    void ognunaGiocaMetaInCasa() {
        var calendario = CalendarioGeneratore.genera(squadre(10), 7);
        int[] casa = new int[11];
        calendario.forEach(g -> g.forEach(a -> casa[(int) a.casaId()]++));
        for (int i = 1; i <= 10; i++) {
            assertThat(casa[i]).isEqualTo(9);
        }
    }

    @Test
    void nessunaSerieDiPiuDiTrePartiteNelloStessoCampo() {
        var calendario = CalendarioGeneratore.genera(squadre(10), 1);
        for (long squadra = 1; squadra <= 10; squadra++) {
            int serie = 0;
            Boolean ultimo = null;
            for (var giornata : calendario) {
                for (var a : giornata) {
                    if (a.casaId() == squadra || a.trasfertaId() == squadra) {
                        boolean inCasa = a.casaId() == squadra;
                        serie = (ultimo != null && ultimo == inCasa) ? serie + 1 : 1;
                        ultimo = inCasa;
                        assertThat(serie).isLessThanOrEqualTo(3);
                    }
                }
            }
        }
    }

    @Test
    void stessoSemeStessoCalendario() {
        assertThat(CalendarioGeneratore.genera(squadre(10), 5))
                .isEqualTo(CalendarioGeneratore.genera(squadre(10), 5));
    }

    @Test
    void dateSonoDomenichePrimaESenzaPausaNatalizia() {
        List<LocalDate> date = CalendarioGeneratore.date(2026, 18);
        assertThat(date).hasSize(18).doesNotHaveDuplicates();
        assertThat(date).allMatch(d -> d.getDayOfWeek() == DayOfWeek.SUNDAY);
        assertThat(date).noneMatch(d -> !d.isBefore(LocalDate.of(2026, 12, 22)) && !d.isAfter(LocalDate.of(2027, 1, 6)));
    }
}
