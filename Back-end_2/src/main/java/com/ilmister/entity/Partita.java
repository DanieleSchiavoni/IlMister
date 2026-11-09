package com.ilmister.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import com.ilmister.entity.enumerazioni.StatoPartita;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "partite")
@Getter
@Setter
@NoArgsConstructor
public class Partita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "giornata_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Giornata giornata;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "squadra_casa_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Squadra squadraCasa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "squadra_trasferta_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Squadra squadraTrasferta;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private StatoPartita stato = StatoPartita.DA_GIOCARE;

    /** Vuoti finché la partita non è conclusa (al termine del primo tempo c'è già il risultato parziale) */
    @Column(name = "gol_casa")
    private Integer golCasa;

    @Column(name = "gol_trasferta")
    private Integer golTrasferta;

    private Integer spettatori;

    /** Il seme dei numeri casuali di questa partita: stesse formazioni e stesso seme, stessa partita */
    @Column(nullable = false)
    private long seme;

    /** Lo stato della partita salvato all'intervallo (testo JSON); vuoto negli altri casi */
    @Lob
    @Column(name = "stato_simulazione", length = 20_000_000)
    private String statoSimulazione;
}
