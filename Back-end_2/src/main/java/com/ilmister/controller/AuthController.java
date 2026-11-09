package com.ilmister.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ilmister.dto.AutenticazioneResponse;
import com.ilmister.dto.LoginRichiesta;
import com.ilmister.dto.RegistrazioneRichiesta;
import com.ilmister.dto.RinnovoRichiesta;
import com.ilmister.dto.UtenteResponse;
import com.ilmister.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticazione", description = "Registrazione, login, rinnovo della sessione e logout")
@SecurityRequirements // queste rotte non richiedono il token
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra un nuovo utente", description = "409 se username o email sono già in uso")
    public UtenteResponse registra(@Valid @RequestBody RegistrazioneRichiesta richiesta) {
        return authService.registra(richiesta);
    }

    @PostMapping("/login")
    @Operation(summary = "Accede con username (o email) e password",
            description = "Restituisce l'access token (15 minuti) e il refresh token (14 giorni, monouso)")
    public AutenticazioneResponse login(@Valid @RequestBody LoginRichiesta richiesta) {
        return authService.login(richiesta);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Cambia un refresh token con una nuova coppia di token",
            description = "Il vecchio refresh token non vale più; riusarlo chiude tutte le sessioni dell'utente")
    public AutenticazioneResponse rinnova(@Valid @RequestBody RinnovoRichiesta richiesta) {
        return authService.rinnova(richiesta.refreshToken());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Chiude la sessione revocando il refresh token")
    public void logout(@Valid @RequestBody RinnovoRichiesta richiesta) {
        authService.logout(richiesta.refreshToken());
    }
}
