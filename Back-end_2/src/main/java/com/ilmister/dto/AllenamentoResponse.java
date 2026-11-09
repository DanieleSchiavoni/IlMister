package com.ilmister.dto;

import java.util.List;

import com.ilmister.entity.enumerazioni.FocusAllenamento;

public record AllenamentoResponse(FocusAllenamento focus, List<OpzioneFocus> opzioni) {

    public record OpzioneFocus(FocusAllenamento valore, String nome, String descrizione) {
    }
}
