-- ---------------------------------------------------------------------------
-- Fase 11: allenamento (focus della carriera) e movimenti finanziari della squadra dell'utente
-- ---------------------------------------------------------------------------
ALTER TABLE carriere ADD COLUMN focus_allenamento VARCHAR(20) NOT NULL DEFAULT 'EQUILIBRATO';

CREATE TABLE movimenti_finanziari (
    id BIGINT NOT NULL AUTO_INCREMENT,
    carriera_id BIGINT NOT NULL,
    squadra_id BIGINT NOT NULL,
    data_movimento DATE NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    importo DECIMAL(14,2) NOT NULL,
    descrizione VARCHAR(120) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_movimenti_squadra (squadra_id, data_movimento),
    CONSTRAINT fk_movimenti_carriera FOREIGN KEY (carriera_id) REFERENCES carriere (id) ON DELETE CASCADE,
    CONSTRAINT fk_movimenti_squadra FOREIGN KEY (squadra_id) REFERENCES squadre (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
