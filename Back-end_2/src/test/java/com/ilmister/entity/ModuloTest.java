package com.ilmister.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.ilmister.entity.enumerazioni.Modulo;
import com.ilmister.entity.enumerazioni.Posizione;
import com.ilmister.entity.enumerazioni.Ruolo;

class ModuloTest {

    @Test
    void ogniModuloHaUndiciPostiEUnSoloPortiereAlPostoZero() {
        for (Modulo modulo : Modulo.values()) {
            assertThat(modulo.getPosti()).as(modulo.name()).hasSize(Modulo.TITOLARI);
            assertThat(modulo.getPosti().get(0)).as(modulo.name()).isEqualTo(Posizione.PORTIERE);
            assertThat(modulo.numeroPosti(Ruolo.PORTIERE)).as(modulo.name()).isEqualTo(1);
        }
    }

    @Test
    void ilCodiceDelModuloSommaDieciGiocatoriDiCampo() {
        for (Modulo modulo : Modulo.values()) {
            int somma = Arrays.stream(modulo.getCodice().split("-")).mapToInt(Integer::parseInt).sum();
            assertThat(somma).as(modulo.getCodice()).isEqualTo(10);
        }
    }

    @Test
    void iCodiciDeiModuliSonoTuttiDiversi() {
        Set<String> codici = Arrays.stream(Modulo.values()).map(Modulo::getCodice).collect(Collectors.toSet());

        assertThat(codici).hasSize(Modulo.values().length);
    }

    @Test
    void iPostiPerRepartoCorrispondonoAlCodiceDelModulo() {
        assertReparti(Modulo.M_4_3_3, 4, 3, 3);
        assertReparti(Modulo.M_4_4_2, 4, 4, 2);
        assertReparti(Modulo.M_3_5_2, 3, 5, 2);
        assertReparti(Modulo.M_3_4_3, 3, 4, 3);
        assertReparti(Modulo.M_5_3_2, 5, 3, 2);
        // Nel 4-2-3-1 le due ali contano come attaccanti: 4 difensori, 3 centrocampisti, 3 attaccanti
        assertReparti(Modulo.M_4_2_3_1, 4, 3, 3);
    }

    private void assertReparti(Modulo modulo, int difensori, int centrocampisti, int attaccanti) {
        assertThat(modulo.numeroPosti(Ruolo.DIFENSORE)).as(modulo.getCodice() + " difensori").isEqualTo(difensori);
        assertThat(modulo.numeroPosti(Ruolo.CENTROCAMPISTA)).as(modulo.getCodice() + " centrocampisti")
                .isEqualTo(centrocampisti);
        assertThat(modulo.numeroPosti(Ruolo.ATTACCANTE)).as(modulo.getCodice() + " attaccanti").isEqualTo(attaccanti);
    }
}
