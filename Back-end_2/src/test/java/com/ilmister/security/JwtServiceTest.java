package com.ilmister.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import com.ilmister.entity.Utente;
import com.ilmister.entity.enumerazioni.RuoloUtente;

class JwtServiceTest {

    private static final String SEGRETO = "segreto-di-prova-lungo-almeno-trentadue-caratteri";
    private static final Instant ORA = Instant.parse("2026-11-09T10:00:00Z");

    private static Clock orologio(Instant istante) {
        return Clock.fixed(istante, ZoneOffset.UTC);
    }

    private static Utente utente() {
        Utente utente = new Utente();
        utente.setId(42L);
        utente.setUsername("mario");
        utente.setRuolo(RuoloUtente.ADMIN);
        return utente;
    }

    @Test
    void ilTokenGeneratoSiRileggeConIDatiDellUtente() {
        JwtService servizio = new JwtService(SEGRETO, 15, orologio(ORA));

        UtenteAutenticato letto = servizio.leggi(servizio.genera(utente())).orElseThrow();

        assertThat(letto).isEqualTo(new UtenteAutenticato(42L, "mario", RuoloUtente.ADMIN));
    }

    @Test
    void unTokenScadutoNonValePiu() {
        String token = new JwtService(SEGRETO, 15, orologio(ORA)).genera(utente());

        JwtService sedicimMinutiDopo = new JwtService(SEGRETO, 15, orologio(ORA.plus(Duration.ofMinutes(16))));
        JwtService cinqueMinutiDopo = new JwtService(SEGRETO, 15, orologio(ORA.plus(Duration.ofMinutes(5))));

        assertThat(sedicimMinutiDopo.leggi(token)).isEmpty();
        assertThat(cinqueMinutiDopo.leggi(token)).isPresent();
    }

    @Test
    void unTokenFirmatoConUnAltroSegretoNonVale() {
        String token = new JwtService("un-altro-segreto-lungo-almeno-trentadue-caratteri-x", 15, orologio(ORA))
                .genera(utente());

        assertThat(new JwtService(SEGRETO, 15, orologio(ORA)).leggi(token)).isEmpty();
    }

    @Test
    void unTokenManomessoNonVale() {
        JwtService servizio = new JwtService(SEGRETO, 15, orologio(ORA));
        String token = servizio.genera(utente());
        // Si cambia un carattere nel mezzo del contenuto
        int posizione = token.indexOf('.') + 5;
        char sostituto = token.charAt(posizione) == 'A' ? 'B' : 'A';
        String manomesso = token.substring(0, posizione) + sostituto + token.substring(posizione + 1);

        assertThat(servizio.leggi(manomesso)).isEmpty();
    }

    @Test
    void testoQualunqueNonEUnTokenValido() {
        JwtService servizio = new JwtService(SEGRETO, 15, orologio(ORA));

        assertThat(servizio.leggi("")).isEmpty();
        assertThat(servizio.leggi("non-un-token")).isEmpty();
        assertThat(servizio.leggi("aaa.bbb.ccc")).isEmpty();
    }

    @Test
    void unSegretoTroppoCortoImpedisceLAvvio() {
        assertThatThrownBy(() -> new JwtService("corto", 15, orologio(ORA)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("JWT_SECRET");
        assertThatThrownBy(() -> new JwtService("", 15, orologio(ORA))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void laDurataDelTokenEQuellaConfigurata() {
        assertThat(new JwtService(SEGRETO, 15, orologio(ORA)).durataInSecondi()).isEqualTo(900);
    }
}
