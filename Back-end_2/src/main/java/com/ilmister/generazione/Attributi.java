package com.ilmister.generazione;

/** I valori (da 1 a 99) dei dieci attributi di un calciatore */
public record Attributi(int velocita, int resistenza, int forza, int tecnica, int passaggio, int tiro,
        int dribbling, int contrasto, int intelligenza, int parate) {

    /** I valori nell'ordine dell'enum Attributo */
    public int[] comeVettore() {
        return new int[] { velocita, resistenza, forza, tecnica, passaggio, tiro, dribbling, contrasto,
                intelligenza, parate };
    }

    public static Attributi daVettore(int[] v) {
        return new Attributi(v[0], v[1], v[2], v[3], v[4], v[5], v[6], v[7], v[8], v[9]);
    }
}
