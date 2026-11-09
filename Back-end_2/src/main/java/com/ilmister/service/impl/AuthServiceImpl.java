package com.ilmister.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ilmister.dto.AutenticazioneResponse;
import com.ilmister.dto.LoginRichiesta;
import com.ilmister.dto.RegistrazioneRichiesta;
import com.ilmister.dto.UtenteResponse;
import com.ilmister.entity.RefreshToken;
import com.ilmister.entity.Utente;
import com.ilmister.exception.AccountBloccatoException;
import com.ilmister.exception.ConflittoException;
import com.ilmister.exception.CredenzialiNonValideException;
import com.ilmister.repository.RefreshTokenRepository;
import com.ilmister.repository.UtenteRepository;
import com.ilmister.security.JwtService;
import com.ilmister.service.AuthService;

@Service
public class AuthServiceImpl implements AuthService {

    private static final String MESSAGGIO_CREDENZIALI = "Credenziali non valide";
    private static final String MESSAGGIO_SESSIONE = "Sessione non valida: effettua di nuovo il login";

    private final UtenteRepository utenteRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final Clock clock;
    private final Duration durataRefresh;
    private final SecureRandom casuale = new SecureRandom();
    /** Serve a spendere lo stesso tempo anche quando l'utente non esiste, così il login non rivela chi è registrato */
    private final String hashFinto;

    public AuthServiceImpl(UtenteRepository utenteRepository, RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder, JwtService jwtService, Clock clock,
            @Value("${app.jwt.durata-refresh-giorni:14}") long durataRefreshGiorni) {
        this.utenteRepository = utenteRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.clock = clock;
        this.durataRefresh = Duration.ofDays(durataRefreshGiorni);
        this.hashFinto = passwordEncoder.encode("password-finta-per-il-confronto");
    }

    @Override
    @Transactional
    public UtenteResponse registra(RegistrazioneRichiesta richiesta) {
        String username = richiesta.username().trim();
        String email = richiesta.email().trim().toLowerCase();
        if (utenteRepository.existsByUsernameIgnoreCase(username)) {
            throw new ConflittoException("Username già in uso");
        }
        if (utenteRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflittoException("Email già registrata");
        }
        Utente utente = new Utente();
        utente.setUsername(username);
        utente.setEmail(email);
        utente.setPassword(passwordEncoder.encode(richiesta.password()));
        return aResponse(utenteRepository.save(utente));
    }

    @Override
    @Transactional
    public AutenticazioneResponse login(LoginRichiesta richiesta) {
        String identificativo = richiesta.identificativo().trim();
        Optional<Utente> trovato = utenteRepository.findByUsernameIgnoreCase(identificativo)
                .or(() -> utenteRepository.findByEmailIgnoreCase(identificativo));

        boolean passwordCorretta = passwordEncoder.matches(richiesta.password(),
                trovato.map(Utente::getPassword).orElse(hashFinto));
        if (trovato.isEmpty() || !passwordCorretta) {
            throw new CredenzialiNonValideException(MESSAGGIO_CREDENZIALI);
        }
        Utente utente = trovato.get();
        if (!utente.isAbilitato()) {
            throw new AccountBloccatoException();
        }
        return emettiToken(utente);
    }

    /**
     * Il refresh token è monouso: ogni rinnovo lo revoca e ne consegna uno nuovo. Se qualcuno presenta un token
     * già revocato (forse rubato) si chiudono tutte le sessioni dell'utente. Per questo l'eccezione non annulla
     * la transazione: la chiusura delle sessioni deve restare.
     */
    @Override
    @Transactional(noRollbackFor = CredenzialiNonValideException.class)
    public AutenticazioneResponse rinnova(String refreshToken) {
        RefreshToken salvato = refreshTokenRepository.findByHashToken(impronta(refreshToken))
                .orElseThrow(() -> new CredenzialiNonValideException(MESSAGGIO_SESSIONE));
        Utente utente = salvato.getUtente();
        if (salvato.isRevocato()) {
            refreshTokenRepository.eliminaPerUtente(utente.getId());
            throw new CredenzialiNonValideException(MESSAGGIO_SESSIONE);
        }
        if (salvato.getScadenza().isBefore(LocalDateTime.now(clock))) {
            throw new CredenzialiNonValideException(MESSAGGIO_SESSIONE);
        }
        if (!utente.isAbilitato()) {
            throw new AccountBloccatoException();
        }
        salvato.setRevocato(true);
        return emettiToken(utente);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByHashToken(impronta(refreshToken)).ifPresent(token -> token.setRevocato(true));
    }

    private AutenticazioneResponse emettiToken(Utente utente) {
        byte[] byteCasuali = new byte[32];
        casuale.nextBytes(byteCasuali);
        String refreshToken = Base64.getUrlEncoder().withoutPadding().encodeToString(byteCasuali);

        RefreshToken salvato = new RefreshToken();
        salvato.setUtente(utente);
        salvato.setHashToken(impronta(refreshToken));
        salvato.setScadenza(LocalDateTime.now(clock).plus(durataRefresh));
        refreshTokenRepository.save(salvato);

        return new AutenticazioneResponse(jwtService.genera(utente), refreshToken, "Bearer",
                jwtService.durataInSecondi(), aResponse(utente));
    }

    /** Nel database c'è solo l'impronta SHA-256 del token: chi legge la tabella non può usarlo */
    static String impronta(String token) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 non disponibile", e);
        }
    }

    static UtenteResponse aResponse(Utente utente) {
        return new UtenteResponse(utente.getId(), utente.getUsername(), utente.getEmail(), utente.getRuolo(),
                utente.isAbilitato(), utente.getDataCreazione());
    }
}
