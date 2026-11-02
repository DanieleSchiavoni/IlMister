package com.ilmister.generazione;

import java.util.List;

/** Gli elenchi da cui il generatore pesca nomi di persone, squadre, città e colori */
final class NomiItaliani {

    static final List<String> NOMI = List.of(
            "Luca", "Marco", "Andrea", "Matteo", "Giuseppe", "Francesco", "Alessandro", "Davide", "Simone",
            "Federico", "Lorenzo", "Riccardo", "Stefano", "Antonio", "Giovanni", "Mattia", "Nicola", "Daniele",
            "Michele", "Roberto", "Fabio", "Gabriele", "Paolo", "Giacomo", "Emanuele", "Salvatore", "Vincenzo",
            "Alberto", "Claudio", "Dario", "Domenico", "Enrico", "Filippo", "Gianluca", "Massimo", "Pietro",
            "Raffaele", "Samuele", "Tommaso", "Valerio", "Christian", "Edoardo", "Leonardo", "Manuel", "Niccolò",
            "Cristian", "Diego", "Elia", "Fabrizio", "Gianmarco", "Giorgio", "Ivan", "Jacopo", "Leandro",
            "Mario", "Mirko", "Nunzio", "Orazio", "Pasquale", "Ruggero", "Sergio", "Thomas", "Umberto",
            "Valentino", "Yuri", "Angelo", "Biagio", "Carmine", "Cosimo", "Donato", "Egidio", "Ettore",
            "Flavio", "Gaetano", "Ignazio", "Lucio", "Maurizio", "Nello", "Osvaldo", "Rocco", "Silvio",
            "Tiziano", "Vito");

    static final List<String> COGNOMI = List.of(
            "Rossi", "Russo", "Ferrari", "Esposito", "Bianchi", "Romano", "Colombo", "Ricci", "Marino", "Greco",
            "Bruno", "Gallo", "Conti", "De Luca", "Mancini", "Costa", "Giordano", "Rizzo", "Lombardi", "Moretti",
            "Barbieri", "Fontana", "Santoro", "Mariani", "Rinaldi", "Caruso", "Ferrara", "Galli", "Martini",
            "Leone", "Longo", "Gentile", "Martinelli", "Vitale", "Serra", "Coppola", "De Santis", "D'Angelo",
            "Marchetti", "Parisi", "Villa", "Conte", "Ferraro", "Ferri", "Fabbri", "Marini", "Grasso", "Valentini",
            "Messina", "Sala", "De Angelis", "Gatti", "Pellegrini", "Palumbo", "Sanna", "Farina", "Rizzi", "Monti",
            "Cattaneo", "Morelli", "Amato", "Silvestri", "Mazza", "Testa", "Grassi", "Pellegrino", "Carbone",
            "Giuliani", "Benedetti", "Barone", "Rossetti", "Caputo", "Montanari", "Guerra", "Palmieri", "Bernardi",
            "Martino", "Fiore", "De Rosa", "Ferretti", "Bellini", "Basile", "Riva", "Donati", "Piras", "Vitali",
            "Battaglia", "Sartori", "Neri", "Costantini", "Milani", "Pagano", "Ruggiero", "Sorrentino", "D'Amico",
            "Orlando", "Quaranta", "Pezzolla", "Latorre", "Perrone", "Tarantino", "Cacciatore", "Capone", "Lippolis",
            "Margheriti", "Zizzi", "Carrozzo", "Leo", "Miccoli", "Stasi", "Lacaita", "Notaro", "Pichierri",
            "Scarciglia", "Tondo", "Vergine", "Greco Maggio", "Aprile", "Bello", "Cavallo", "Dell'Aglio", "Elia",
            "Fanelli", "Gioia", "Marangi");

    /** Città inventate: così nessuna squadra del gioco coincide con un club reale */
    static final List<String> CITTA = List.of(
            "Valmora", "Castelverde Alta", "Borgoalto", "Pietrarossa", "Montesole", "Lagonero", "Rivachiara",
            "Terranova Alta", "San Floriano", "Campodoro Nuova", "Roccabianca Marina", "Fontanalba", "Portovivo",
            "Collalto Scalo", "Serravalle Nuova", "Vallechiara", "Monteverdi Basso", "San Teodoro Mare",
            "Borgonuovo", "Cerreto Alto", "Villaserena", "Pontelungo", "Santa Lucilla", "Rocca d'Argento");

    static final List<String> PREFISSI = List.of(
            "Atletico", "Sporting", "Virtus", "Audax", "Olimpia", "Polisportiva", "Pro", "Unione Sportiva",
            "Real", "Libertas", "Fortitudo", "Gioventù");

    static final List<String> TIPI_STADIO = List.of(
            "Stadio Comunale", "Campo Sportivo", "Stadio Municipale", "Centro Sportivo");

    /** Coppie di colori (maglia, dettagli) */
    static final List<String[]> COLORI = List.of(
            new String[] { "#c0392b", "#ffffff" }, new String[] { "#1f4e9c", "#ffffff" },
            new String[] { "#2e7d32", "#ffffff" }, new String[] { "#f9a825", "#1a1a1a" },
            new String[] { "#6a1b9a", "#ffffff" }, new String[] { "#00838f", "#ffffff" },
            new String[] { "#212121", "#ffffff" }, new String[] { "#ef6c00", "#1a1a1a" },
            new String[] { "#ad1457", "#ffffff" }, new String[] { "#3949ab", "#ffeb3b" },
            new String[] { "#00695c", "#ffffff" }, new String[] { "#795548", "#ffffff" });

    private NomiItaliani() {
    }
}
