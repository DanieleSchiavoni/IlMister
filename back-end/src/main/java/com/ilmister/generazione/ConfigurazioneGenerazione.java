package com.ilmister.generazione;

/**
 * I parametri del campionato da generare.
 *
 * @param nomeCampionato il nome del campionato
 * @param numeroSquadre  un numero pari, da 4 a 20 (serve pari per il calendario)
 * @param livelloMedio   la valutazione media di una squadra di reputazione media: più è alta, più il campionato è forte
 */
public record ConfigurazioneGenerazione(String nomeCampionato, int numeroSquadre, int livelloMedio) {

    public static final int GIOCATORI_PER_SQUADRA = 26;

    public ConfigurazioneGenerazione {
        if (numeroSquadre < 4 || numeroSquadre > 20 || numeroSquadre % 2 != 0) {
            throw new IllegalArgumentException("Il numero di squadre deve essere pari, da 4 a 20");
        }
        if (livelloMedio < 30 || livelloMedio > 80) {
            throw new IllegalArgumentException("Il livello medio deve essere compreso tra 30 e 80");
        }
    }

    public static ConfigurazioneGenerazione predefinita() {
        return new ConfigurazioneGenerazione("Campionato Regionale", 10, 58);
    }
}
