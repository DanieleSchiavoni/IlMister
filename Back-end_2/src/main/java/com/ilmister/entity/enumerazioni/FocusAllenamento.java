package com.ilmister.entity.enumerazioni;

/** Su cosa si allena la squadra dell'utente: a ogni giornata i giocatori migliorano un po' gli attributi di quel tipo */
public enum FocusAllenamento {
    EQUILIBRATO("Equilibrato", "Un po' di tutto"),
    FISICO("Fisico", "Velocità, resistenza e forza"),
    TECNICO("Tecnico", "Tecnica, passaggio e dribbling"),
    DIFENSIVO("Difensivo", "Contrasto, intelligenza tattica, forza e parate"),
    OFFENSIVO("Offensivo", "Tiro, dribbling e velocità");

    private final String nome;
    private final String descrizione;

    FocusAllenamento(String nome, String descrizione) {
        this.nome = nome;
        this.descrizione = descrizione;
    }

    public String getNome() {
        return nome;
    }

    public String getDescrizione() {
        return descrizione;
    }
}
