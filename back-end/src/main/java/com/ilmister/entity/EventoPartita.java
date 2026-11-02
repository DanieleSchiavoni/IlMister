package com.ilmister.entity;

import java.math.BigDecimal;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import com.ilmister.entity.enumerazioni.TipoEvento;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Una riga della cronaca di una partita */
@Entity
@Table(name = "eventi_partita",
        uniqueConstraints = @UniqueConstraint(name = "uq_eventi_partita_ordine", columnNames = { "partita_id", "ordine" }))
@Getter
@Setter
@NoArgsConstructor
public class EventoPartita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partita_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Partita partita;

    /** La posizione dell'evento nella cronaca, da 1 in poi */
    @Column(nullable = false)
    private int ordine;

    @Column(nullable = false)
    private int minuto;

    @Column(name = "minuto_recupero", nullable = false)
    private int minutoRecupero = 0;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private TipoEvento tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "squadra_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Squadra squadra;

    /** Il protagonista dell'evento (chi tira, chi segna, chi viene ammonito, chi esce...) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "giocatore_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Giocatore giocatore;

    /** Il secondo giocatore coinvolto (chi fa l'assist, chi entra al posto di un altro...) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "secondo_giocatore_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Giocatore secondoGiocatore;

    /** I gol attesi dell'azione, solo per le conclusioni a rete */
    @Column(precision = 4, scale = 2)
    private BigDecimal xg;

    @Column(nullable = false, length = 400)
    private String testo;
}
