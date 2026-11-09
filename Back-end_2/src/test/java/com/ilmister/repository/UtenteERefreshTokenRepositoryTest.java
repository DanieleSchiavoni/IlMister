package com.ilmister.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ilmister.entity.RefreshToken;
import com.ilmister.entity.Utente;

class UtenteERefreshTokenRepositoryTest extends RepositoryTestBase {

    @Autowired
    private UtenteRepository utenteRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private Utente mario;
    private Utente luca;

    @BeforeEach
    void preparaDati() {
        mario = creaUtente("Mario");
        luca = creaUtente("Luca");
    }

    private RefreshToken creaToken(Utente utente, char carattere, LocalDateTime scadenza) {
        RefreshToken token = new RefreshToken();
        token.setUtente(utente);
        token.setHashToken(String.valueOf(carattere).repeat(64)); // un'impronta SHA-256 è lunga 64 caratteri
        token.setScadenza(scadenza);
        return em.persistAndFlush(token);
    }

    @Test
    void laRicercaDiUsernameEEmailIgnoraMaiuscoleEMinuscole() {
        assertThat(utenteRepository.findByUsernameIgnoreCase("MARIO")).isPresent();
        assertThat(utenteRepository.findByEmailIgnoreCase("LUCA@TEST.IT")).isPresent();
        assertThat(utenteRepository.existsByUsernameIgnoreCase("mArIo")).isTrue();
        assertThat(utenteRepository.existsByEmailIgnoreCase("nessuno@test.it")).isFalse();
    }

    @Test
    void unTokenSiTrovaDalSuoHash() {
        creaToken(mario, 'a', LocalDateTime.now().plusDays(7));

        assertThat(refreshTokenRepository.findByHashToken("a".repeat(64))).isPresent();
        assertThat(refreshTokenRepository.findByHashToken("b".repeat(64))).isEmpty();
    }

    @Test
    void chiudereLeSessioniDiUnUtenteNonToccaQuelleDegliAltri() {
        creaToken(mario, 'a', LocalDateTime.now().plusDays(7));
        creaToken(mario, 'b', LocalDateTime.now().plusDays(7));
        creaToken(luca, 'c', LocalDateTime.now().plusDays(7));

        int eliminati = refreshTokenRepository.eliminaPerUtente(mario.getId());

        assertThat(eliminati).isEqualTo(2);
        assertThat(refreshTokenRepository.count()).isEqualTo(1);
    }

    @Test
    void laPuliziaEliminaSoloITokenScaduti() {
        creaToken(mario, 'a', LocalDateTime.now().minusDays(1));
        creaToken(mario, 'b', LocalDateTime.now().plusDays(1));

        int eliminati = refreshTokenRepository.eliminaScaduti(LocalDateTime.now());

        assertThat(eliminati).isEqualTo(1);
        assertThat(refreshTokenRepository.findByHashToken("b".repeat(64))).isPresent();
    }
}
