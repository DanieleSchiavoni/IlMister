-- Il Mister - schema iniziale
-- Tutti i dati di gioco appartengono a una CARRIERA (un salvataggio): cancellando la carriera si cancella tutto.
-- Gli enum sono salvati come testo (VARCHAR) con il nome della costante Java.

-- ---------------------------------------------------------------------------
-- Utenti e sessioni
-- ---------------------------------------------------------------------------
CREATE TABLE utenti (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(120) NOT NULL,
    password VARCHAR(100) NOT NULL,
    ruolo VARCHAR(20) NOT NULL DEFAULT 'UTENTE',
    abilitato BOOLEAN NOT NULL DEFAULT TRUE,
    data_creazione DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uq_utenti_username UNIQUE (username),
    CONSTRAINT uq_utenti_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE refresh_token (
    id BIGINT NOT NULL AUTO_INCREMENT,
    utente_id BIGINT NOT NULL,
    hash_token CHAR(64) NOT NULL,
    scadenza DATETIME(6) NOT NULL,
    revocato BOOLEAN NOT NULL DEFAULT FALSE,
    data_creazione DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uq_refresh_token_hash UNIQUE (hash_token),
    CONSTRAINT fk_refresh_token_utente FOREIGN KEY (utente_id) REFERENCES utenti (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- Carriere (salvataggi)
-- La squadra allenata e la stagione in corso NON sono colonne di questa tabella: sono segnate
-- su squadre (allenata_dall_utente) e stagioni (stato = 'IN_CORSO'). Cosi non ci sono
-- riferimenti circolari tra tabelle, che con ON DELETE CASCADE darebbero problemi in MySQL.
-- ---------------------------------------------------------------------------
CREATE TABLE carriere (
    id BIGINT NOT NULL AUTO_INCREMENT,
    utente_id BIGINT NOT NULL,
    nome VARCHAR(80) NOT NULL,
    nome_allenatore VARCHAR(80) NOT NULL,
    data_corrente DATE NOT NULL,
    fiducia_presidente INT NOT NULL DEFAULT 60,
    stato VARCHAR(20) NOT NULL DEFAULT 'IN_CORSO',
    seme BIGINT NOT NULL,
    versione BIGINT NOT NULL DEFAULT 0,
    data_creazione DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    data_aggiornamento DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_carriere_utente FOREIGN KEY (utente_id) REFERENCES utenti (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE stagioni (
    id BIGINT NOT NULL AUTO_INCREMENT,
    carriera_id BIGINT NOT NULL,
    anno_inizio INT NOT NULL,
    data_inizio DATE NOT NULL,
    data_fine DATE NOT NULL,
    stato VARCHAR(20) NOT NULL DEFAULT 'IN_CORSO',
    PRIMARY KEY (id),
    CONSTRAINT uq_stagioni_anno UNIQUE (carriera_id, anno_inizio),
    CONSTRAINT fk_stagioni_carriera FOREIGN KEY (carriera_id) REFERENCES carriere (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Un campionato (livello 1 = il piu alto). Nella versione 1 ce n'e uno solo;
-- la struttura permette piu livelli e gironi (promozioni e retrocessioni) in futuro.
CREATE TABLE campionati (
    id BIGINT NOT NULL AUTO_INCREMENT,
    carriera_id BIGINT NOT NULL,
    nome VARCHAR(80) NOT NULL,
    livello INT NOT NULL DEFAULT 1,
    girone VARCHAR(10) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_campionati_nome UNIQUE (carriera_id, nome),
    CONSTRAINT fk_campionati_carriera FOREIGN KEY (carriera_id) REFERENCES carriere (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- Squadre e giocatori
-- ---------------------------------------------------------------------------
CREATE TABLE squadre (
    id BIGINT NOT NULL AUTO_INCREMENT,
    carriera_id BIGINT NOT NULL,
    campionato_id BIGINT NOT NULL,
    nome VARCHAR(80) NOT NULL,
    nome_breve VARCHAR(30) NOT NULL,
    citta VARCHAR(60) NULL,
    stadio VARCHAR(80) NOT NULL,
    capienza INT NOT NULL,
    reputazione INT NOT NULL,
    colore_primario VARCHAR(7) NOT NULL DEFAULT '#0d6efd',
    colore_secondario VARCHAR(7) NOT NULL DEFAULT '#ffffff',
    budget_trasferimenti DECIMAL(14,2) NOT NULL DEFAULT 0,
    allenata_dall_utente BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    CONSTRAINT uq_squadre_nome UNIQUE (carriera_id, nome),
    CONSTRAINT fk_squadre_carriera FOREIGN KEY (carriera_id) REFERENCES carriere (id) ON DELETE CASCADE,
    CONSTRAINT fk_squadre_campionato FOREIGN KEY (campionato_id) REFERENCES campionati (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- squadra_id NULL = giocatore svincolato.
-- Gli attributi vanno da 1 a 100; i controlli di coerenza sono nel codice.
CREATE TABLE giocatori (
    id BIGINT NOT NULL AUTO_INCREMENT,
    carriera_id BIGINT NOT NULL,
    squadra_id BIGINT NULL,
    nome VARCHAR(60) NOT NULL,
    cognome VARCHAR(60) NOT NULL,
    data_nascita DATE NOT NULL,
    nazionalita VARCHAR(40) NOT NULL DEFAULT 'Italia',
    posizione VARCHAR(30) NOT NULL,
    piede VARCHAR(15) NOT NULL DEFAULT 'DESTRO',
    numero_maglia INT NULL,
    velocita INT NOT NULL,
    resistenza INT NOT NULL,
    forza INT NOT NULL,
    tecnica INT NOT NULL,
    passaggio INT NOT NULL,
    tiro INT NOT NULL,
    dribbling INT NOT NULL,
    contrasto INT NOT NULL,
    intelligenza INT NOT NULL,
    parate INT NOT NULL,
    valutazione INT NOT NULL,
    potenziale INT NOT NULL,
    forma INT NOT NULL DEFAULT 50,
    morale INT NOT NULL DEFAULT 70,
    energia INT NOT NULL DEFAULT 100,
    infortunato_fino_al DATE NULL,
    tipo_infortunio VARCHAR(60) NULL,
    squalifica_giornate INT NOT NULL DEFAULT 0,
    stipendio_annuo DECIMAL(12,2) NOT NULL,
    scadenza_contratto DATE NOT NULL,
    valore_mercato DECIMAL(14,2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_giocatori_carriera FOREIGN KEY (carriera_id) REFERENCES carriere (id) ON DELETE CASCADE,
    CONSTRAINT fk_giocatori_squadra FOREIGN KEY (squadra_id) REFERENCES squadre (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- Calendario, partite e classifica
-- ---------------------------------------------------------------------------
CREATE TABLE giornate (
    id BIGINT NOT NULL AUTO_INCREMENT,
    stagione_id BIGINT NOT NULL,
    campionato_id BIGINT NOT NULL,
    numero INT NOT NULL,
    data_giornata DATE NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_giornate_numero UNIQUE (stagione_id, campionato_id, numero),
    CONSTRAINT fk_giornate_stagione FOREIGN KEY (stagione_id) REFERENCES stagioni (id) ON DELETE CASCADE,
    CONSTRAINT fk_giornate_campionato FOREIGN KEY (campionato_id) REFERENCES campionati (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- stato_simulazione: lo stato della partita salvato all'intervallo (testo JSON), vuoto negli altri casi.
CREATE TABLE partite (
    id BIGINT NOT NULL AUTO_INCREMENT,
    giornata_id BIGINT NOT NULL,
    squadra_casa_id BIGINT NOT NULL,
    squadra_trasferta_id BIGINT NOT NULL,
    stato VARCHAR(20) NOT NULL DEFAULT 'DA_GIOCARE',
    gol_casa INT NULL,
    gol_trasferta INT NULL,
    spettatori INT NULL,
    seme BIGINT NOT NULL,
    stato_simulazione LONGTEXT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_partite_giornata FOREIGN KEY (giornata_id) REFERENCES giornate (id) ON DELETE CASCADE,
    CONSTRAINT fk_partite_casa FOREIGN KEY (squadra_casa_id) REFERENCES squadre (id) ON DELETE CASCADE,
    CONSTRAINT fk_partite_trasferta FOREIGN KEY (squadra_trasferta_id) REFERENCES squadre (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- La cronaca: un evento per riga, in ordine.
CREATE TABLE eventi_partita (
    id BIGINT NOT NULL AUTO_INCREMENT,
    partita_id BIGINT NOT NULL,
    ordine INT NOT NULL,
    minuto INT NOT NULL,
    minuto_recupero INT NOT NULL DEFAULT 0,
    tipo VARCHAR(30) NOT NULL,
    squadra_id BIGINT NULL,
    giocatore_id BIGINT NULL,
    secondo_giocatore_id BIGINT NULL,
    xg DECIMAL(4,2) NULL,
    testo VARCHAR(400) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_eventi_partita_ordine UNIQUE (partita_id, ordine),
    CONSTRAINT fk_eventi_partita FOREIGN KEY (partita_id) REFERENCES partite (id) ON DELETE CASCADE,
    CONSTRAINT fk_eventi_squadra FOREIGN KEY (squadra_id) REFERENCES squadre (id) ON DELETE CASCADE,
    CONSTRAINT fk_eventi_giocatore FOREIGN KEY (giocatore_id) REFERENCES giocatori (id) ON DELETE CASCADE,
    CONSTRAINT fk_eventi_secondo_giocatore FOREIGN KEY (secondo_giocatore_id) REFERENCES giocatori (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE statistiche_partita (
    id BIGINT NOT NULL AUTO_INCREMENT,
    partita_id BIGINT NOT NULL,
    squadra_id BIGINT NOT NULL,
    possesso INT NOT NULL,
    tiri INT NOT NULL,
    tiri_in_porta INT NOT NULL,
    xg DECIMAL(5,2) NOT NULL,
    falli INT NOT NULL,
    calci_d_angolo INT NOT NULL,
    ammonizioni INT NOT NULL,
    espulsioni INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_statistiche_partita UNIQUE (partita_id, squadra_id),
    CONSTRAINT fk_statistiche_partita FOREIGN KEY (partita_id) REFERENCES partite (id) ON DELETE CASCADE,
    CONSTRAINT fk_statistiche_squadra FOREIGN KEY (squadra_id) REFERENCES squadre (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE prestazioni_giocatori (
    id BIGINT NOT NULL AUTO_INCREMENT,
    partita_id BIGINT NOT NULL,
    giocatore_id BIGINT NOT NULL,
    squadra_id BIGINT NOT NULL,
    titolare BOOLEAN NOT NULL,
    minuti_giocati INT NOT NULL DEFAULT 0,
    gol INT NOT NULL DEFAULT 0,
    assist INT NOT NULL DEFAULT 0,
    tiri INT NOT NULL DEFAULT 0,
    ammonito BOOLEAN NOT NULL DEFAULT FALSE,
    espulso BOOLEAN NOT NULL DEFAULT FALSE,
    voto DECIMAL(3,1) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_prestazioni_giocatore UNIQUE (partita_id, giocatore_id),
    CONSTRAINT fk_prestazioni_partita FOREIGN KEY (partita_id) REFERENCES partite (id) ON DELETE CASCADE,
    CONSTRAINT fk_prestazioni_giocatore FOREIGN KEY (giocatore_id) REFERENCES giocatori (id) ON DELETE CASCADE,
    CONSTRAINT fk_prestazioni_squadra FOREIGN KEY (squadra_id) REFERENCES squadre (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE classifica (
    id BIGINT NOT NULL AUTO_INCREMENT,
    stagione_id BIGINT NOT NULL,
    campionato_id BIGINT NOT NULL,
    squadra_id BIGINT NOT NULL,
    giocate INT NOT NULL DEFAULT 0,
    vittorie INT NOT NULL DEFAULT 0,
    pareggi INT NOT NULL DEFAULT 0,
    sconfitte INT NOT NULL DEFAULT 0,
    gol_fatti INT NOT NULL DEFAULT 0,
    gol_subiti INT NOT NULL DEFAULT 0,
    punti INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uq_classifica_squadra UNIQUE (stagione_id, squadra_id),
    CONSTRAINT fk_classifica_stagione FOREIGN KEY (stagione_id) REFERENCES stagioni (id) ON DELETE CASCADE,
    CONSTRAINT fk_classifica_campionato FOREIGN KEY (campionato_id) REFERENCES campionati (id) ON DELETE CASCADE,
    CONSTRAINT fk_classifica_squadra FOREIGN KEY (squadra_id) REFERENCES squadre (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- Formazione e tattica di ogni squadra
-- posto: da 0 a 10 i titolari (nell'ordine dei ruoli del modulo), da 11 in poi la panchina.
-- ---------------------------------------------------------------------------
CREATE TABLE formazioni (
    id BIGINT NOT NULL AUTO_INCREMENT,
    squadra_id BIGINT NOT NULL,
    modulo VARCHAR(20) NOT NULL,
    mentalita VARCHAR(20) NOT NULL DEFAULT 'EQUILIBRATA',
    pressing VARCHAR(10) NOT NULL DEFAULT 'MEDIO',
    ritmo VARCHAR(10) NOT NULL DEFAULT 'NORMALE',
    linea_difensiva VARCHAR(10) NOT NULL DEFAULT 'MEDIA',
    PRIMARY KEY (id),
    CONSTRAINT uq_formazioni_squadra UNIQUE (squadra_id),
    CONSTRAINT fk_formazioni_squadra FOREIGN KEY (squadra_id) REFERENCES squadre (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE formazione_giocatori (
    id BIGINT NOT NULL AUTO_INCREMENT,
    formazione_id BIGINT NOT NULL,
    giocatore_id BIGINT NOT NULL,
    posto INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_formazione_giocatore UNIQUE (formazione_id, giocatore_id),
    CONSTRAINT uq_formazione_posto UNIQUE (formazione_id, posto),
    CONSTRAINT fk_formazione_giocatori_formazione FOREIGN KEY (formazione_id) REFERENCES formazioni (id) ON DELETE CASCADE,
    CONSTRAINT fk_formazione_giocatori_giocatore FOREIGN KEY (giocatore_id) REFERENCES giocatori (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
