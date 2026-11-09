package com.ilmister.service;

import com.ilmister.dto.AutenticazioneResponse;
import com.ilmister.dto.LoginRichiesta;
import com.ilmister.dto.RegistrazioneRichiesta;
import com.ilmister.dto.UtenteResponse;

public interface AuthService {

    /** Crea un nuovo utente (ruolo UTENTE); username ed email devono essere liberi */
    UtenteResponse registra(RegistrazioneRichiesta richiesta);

    AutenticazioneResponse login(LoginRichiesta richiesta);

    /** Cambia un refresh token con una nuova coppia di token: il vecchio non vale più */
    AutenticazioneResponse rinnova(String refreshToken);

    /** Chiude la sessione: il refresh token smette di valere. Non dà errore se è sconosciuto. */
    void logout(String refreshToken);
}
