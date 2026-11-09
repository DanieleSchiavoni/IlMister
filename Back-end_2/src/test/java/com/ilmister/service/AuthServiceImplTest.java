package com.ilmister.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ilmister.dto.AutenticazioneResponse;
import com.ilmister.dto.LoginRichiesta;
import com.ilmister.dto.RegistrazioneRichiesta;
import com.ilmister.dto.UtenteResponse;
import com.ilmister.entity.Utente;
import com.ilmister.entity.enumerazioni.RuoloUtente;
import com.ilmister.exception.AccountBloccatoException;
import com.ilmister.exception.ConflittoException;
import com.ilmister.exception.CredenzialiNonValideException;
import com.ilmister.repository.RefreshTokenRepository;
import com.ilmister.repository.RepositoryTestBase;
import com.ilmister.repository.UtenteRepository;
import com.ilmister.security.JwtService;
import com.ilmister.service.impl.AuthServiceImpl;

@Import({ AuthServiceImpl.class, JwtService.class })
class AuthServiceImplTest extends RepositoryTestBase {

    /** Un orologio che si può spostare in avanti, per provare le scadenze */
    static class OrologioSpostabile extends Clock {
        private Instant istante = Instant.parse("2026-11-09T10:00:00Z");

        void riporta() {
            istante = Instant.parse("2026-11-09T10:00:00Z");
        }

        void avanza(Duration durata) {
            istante = istante.plus(durata);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zona) {
            return this;
        }

        @Override
        public Instant instant() {
            return istante;
        }
    }

    @TestConfiguration
    static class Configurazione {
        @Bean
        OrologioSpostabile orologio() {
            return new OrologioSpostabile();
        }

        // Costo basso: nei test conta la velocità, non la robustezza dell'hash
        @Bean
        PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder(4);
        }
    }

    @Autowired
    private AuthService authService;
    @Autowired
    private UtenteRepository utenteRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private OrologioSpostabile orologio;

    @BeforeEach
    void riportaLOrologio() {
        orologio.riporta();
    }

    private UtenteResponse registraMario() {
        return authService.registra(new RegistrazioneRichiesta("Mario", "Mario@Test.it", "password-sicura"));
    }

    @Test
    void laRegistrazioneSalvaLaPasswordCifrata() {
        UtenteResponse risposta = registraMario();
        em.flush();
        em.clear();

        Utente salvato = utenteRepository.findById(risposta.id()).orElseThrow();
        assertThat(salvato.getPassword()).isNotEqualTo("password-sicura").startsWith("$2");
        assertThat(salvato.getRuolo()).isEqualTo(RuoloUtente.UTENTE);
        assertThat(salvato.isAbilitato()).isTrue();
        assertThat(salvato.getEmail()).isEqualTo("mario@test.it");
    }

    @Test
    void usernameEdEmailNonSiRipetonoNemmenoCambiandoLeMaiuscole() {
        registraMario();

        assertThatThrownBy(() -> authService.registra(new RegistrazioneRichiesta("MARIO", "altra@test.it", "password-sicura")))
                .isInstanceOf(ConflittoException.class).hasMessageContaining("Username");
        assertThatThrownBy(() -> authService.registra(new RegistrazioneRichiesta("luca", "MARIO@test.it", "password-sicura")))
                .isInstanceOf(ConflittoException.class).hasMessageContaining("Email");
    }

    @Test
    void siFaIlLoginConUsernameOConEmail() {
        registraMario();

        AutenticazioneResponse conUsername = authService.login(new LoginRichiesta("mario", "password-sicura"));
        AutenticazioneResponse conEmail = authService.login(new LoginRichiesta("mario@test.it", "password-sicura"));

        assertThat(conUsername.accessToken()).isNotBlank();
        assertThat(conUsername.refreshToken()).isNotBlank();
        assertThat(conUsername.tipo()).isEqualTo("Bearer");
        assertThat(conUsername.scadenzaAccessInSecondi()).isEqualTo(900);
        assertThat(conEmail.utente().username()).isEqualTo("Mario");
    }

    @Test
    void conCredenzialiSbagliateSiRispondeSempreLoStessoErrore() {
        registraMario();

        assertThatThrownBy(() -> authService.login(new LoginRichiesta("mario", "password-sbagliata")))
                .isInstanceOf(CredenzialiNonValideException.class).hasMessage("Credenziali non valide");
        assertThatThrownBy(() -> authService.login(new LoginRichiesta("nessuno", "password-sicura")))
                .isInstanceOf(CredenzialiNonValideException.class).hasMessage("Credenziali non valide");
    }

    @Test
    void unUtenteBloccatoNonPuoEntrare() {
        UtenteResponse mario = registraMario();
        Utente utente = utenteRepository.findById(mario.id()).orElseThrow();
        utente.setAbilitato(false);
        em.persistAndFlush(utente);

        assertThatThrownBy(() -> authService.login(new LoginRichiesta("mario", "password-sicura")))
                .isInstanceOf(AccountBloccatoException.class);
    }

    @Test
    void nelDatabaseCeSoloLImprontaDelRefreshToken() {
        registraMario();

        AutenticazioneResponse login = authService.login(new LoginRichiesta("mario", "password-sicura"));
        em.flush();

        var salvato = refreshTokenRepository.findAll();
        assertThat(salvato).hasSize(1);
        assertThat(salvato.get(0).getHashToken()).hasSize(64).isNotEqualTo(login.refreshToken());
        assertThat(salvato.get(0).isRevocato()).isFalse();
    }

    @Test
    void ilRinnovoDaNuoviTokenERevocaIlVecchio() {
        registraMario();
        AutenticazioneResponse primo = authService.login(new LoginRichiesta("mario", "password-sicura"));

        AutenticazioneResponse secondo = authService.rinnova(primo.refreshToken());
        em.flush();
        em.clear();

        assertThat(secondo.refreshToken()).isNotEqualTo(primo.refreshToken());
        assertThat(secondo.accessToken()).isNotBlank();
        assertThat(refreshTokenRepository.findAll()).hasSize(2);
        assertThat(refreshTokenRepository.findAll().stream().filter(t -> !t.isRevocato())).hasSize(1);
    }

    @Test
    void riusareUnRefreshTokenRevocatoChiudeTutteLeSessioni() {
        registraMario();
        AutenticazioneResponse primo = authService.login(new LoginRichiesta("mario", "password-sicura"));
        AutenticazioneResponse secondo = authService.rinnova(primo.refreshToken());
        em.flush();
        em.clear();

        assertThatThrownBy(() -> authService.rinnova(primo.refreshToken()))
                .isInstanceOf(CredenzialiNonValideException.class);
        em.flush();
        em.clear();

        // Anche il token nuovo, legittimo, è stato chiuso per sicurezza
        assertThat(refreshTokenRepository.count()).isZero();
        assertThatThrownBy(() -> authService.rinnova(secondo.refreshToken()))
                .isInstanceOf(CredenzialiNonValideException.class);
    }

    @Test
    void unRefreshTokenScadutoNonRinnova() {
        registraMario();
        AutenticazioneResponse login = authService.login(new LoginRichiesta("mario", "password-sicura"));

        orologio.avanza(Duration.ofDays(15));

        assertThatThrownBy(() -> authService.rinnova(login.refreshToken()))
                .isInstanceOf(CredenzialiNonValideException.class);
    }

    @Test
    void unRefreshTokenSconosciutoNonRinnova() {
        assertThatThrownBy(() -> authService.rinnova("token-inventato"))
                .isInstanceOf(CredenzialiNonValideException.class);
    }

    @Test
    void dopoIlLogoutIlRefreshTokenNonVale() {
        registraMario();
        AutenticazioneResponse login = authService.login(new LoginRichiesta("mario", "password-sicura"));

        authService.logout(login.refreshToken());
        em.flush();
        em.clear();

        assertThatThrownBy(() -> authService.rinnova(login.refreshToken()))
                .isInstanceOf(CredenzialiNonValideException.class);
    }

    @Test
    void ilLogoutConUnTokenSconosciutoNonDaErrore() {
        authService.logout("token-inventato");
    }
}
