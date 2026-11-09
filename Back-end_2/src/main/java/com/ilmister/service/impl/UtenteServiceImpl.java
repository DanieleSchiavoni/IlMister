package com.ilmister.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ilmister.dto.UtenteResponse;
import com.ilmister.entity.Utente;
import com.ilmister.exception.OperazioneNonValidaException;
import com.ilmister.exception.RisorsaNonTrovataException;
import com.ilmister.repository.RefreshTokenRepository;
import com.ilmister.repository.UtenteRepository;
import com.ilmister.service.UtenteService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UtenteServiceImpl implements UtenteService {

    private final UtenteRepository utenteRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional(readOnly = true)
    public UtenteResponse profilo(Long utenteId) {
        return AuthServiceImpl.aResponse(trova(utenteId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UtenteResponse> elenco() {
        return utenteRepository.findAll().stream().map(AuthServiceImpl::aResponse).toList();
    }

    @Override
    @Transactional
    public UtenteResponse impostaAbilitato(Long adminId, Long utenteId, boolean abilitato) {
        if (adminId.equals(utenteId)) {
            throw new OperazioneNonValidaException("Non puoi bloccare il tuo stesso account");
        }
        Utente utente = trova(utenteId);
        utente.setAbilitato(abilitato);
        if (!abilitato) {
            // Chi è bloccato non può più rinnovare la sessione (l'access token in corso scade in pochi minuti)
            refreshTokenRepository.eliminaPerUtente(utenteId);
        }
        return AuthServiceImpl.aResponse(utenteRepository.save(utente));
    }

    private Utente trova(Long utenteId) {
        return utenteRepository.findById(utenteId)
                .orElseThrow(() -> new RisorsaNonTrovataException("Utente non trovato"));
    }
}
