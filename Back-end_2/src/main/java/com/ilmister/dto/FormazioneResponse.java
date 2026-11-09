package com.ilmister.dto;

import java.util.List;

import com.ilmister.entity.enumerazioni.LineaDifensiva;
import com.ilmister.entity.enumerazioni.Mentalita;
import com.ilmister.entity.enumerazioni.Modulo;
import com.ilmister.entity.enumerazioni.Pressing;
import com.ilmister.entity.enumerazioni.Ritmo;

/**
 * La formazione della squadra dell'utente. Se l'allenatore non ne ha ancora salvata una, "salvata" è falso
 * e la formazione è la proposta automatica.
 */
public record FormazioneResponse(boolean salvata, Modulo modulo, Mentalita mentalita, Pressing pressing, Ritmo ritmo,
        LineaDifensiva lineaDifensiva, List<GiocatoreResponse> titolari, List<GiocatoreResponse> panchina) {
}
