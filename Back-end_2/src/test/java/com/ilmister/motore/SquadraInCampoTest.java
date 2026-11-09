package com.ilmister.motore;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.ilmister.entity.enumerazioni.Modulo;
import com.ilmister.generazione.SquadraGenerata;

class SquadraInCampoTest {

    private final SquadraGenerata generata = SquadreDiProva.campionato(2).get(0);

    @Test
    void servonoEsattamenteUndiciTitolari() {
        FormazioneAutomatica.Scelta scelta = FormazioneAutomatica.scegli(SquadreDiProva.rosa(generata, 0), Modulo.M_4_3_3);
        List<GiocatoreInCampo> dieci = scelta.titolari().subList(0, 10);

        assertThatThrownBy(() -> new SquadraInCampo(1, "Squadra", Modulo.M_4_3_3, Istruzioni.equilibrate(), dieci, scelta.panchina()))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("11 titolari");
    }

    @Test
    void unGiocatoreNonPuoEssereSchieratoDueVolte() {
        FormazioneAutomatica.Scelta scelta = FormazioneAutomatica.scegli(SquadreDiProva.rosa(generata, 0), Modulo.M_4_3_3);
        List<GiocatoreInCampo> conDoppione = new ArrayList<>(scelta.titolari());
        conDoppione.set(10, conDoppione.get(9));

        assertThatThrownBy(() -> new SquadraInCampo(1, "Squadra", Modulo.M_4_3_3, Istruzioni.equilibrate(), conDoppione, scelta.panchina()))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("due volte");
    }

    @Test
    void ognunaDelleQuattroIstruzioniEObbligatoria() {
        assertThatThrownBy(() -> new Istruzioni(null, null, null, null)).isInstanceOf(IllegalArgumentException.class);
    }
}
