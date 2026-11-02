package com.ilmister.entity;

import java.math.BigDecimal;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/** Le statistiche di una squadra in una partita */
@Entity
@Table(name = "statistiche_partita",
        uniqueConstraints = @UniqueConstraint(name = "uq_statistiche_partita", columnNames = { "partita_id", "squadra_id" }))
@Getter
@Setter
@NoArgsConstructor
public class StatisticaPartita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partita_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Partita partita;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "squadra_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Squadra squadra;

    /** Percentuale di possesso palla, da 0 a 100 */
    @Column(nullable = false)
    private int possesso;

    @Column(nullable = false)
    private int tiri;

    @Column(name = "tiri_in_porta", nullable = false)
    private int tiriInPorta;

    /** I gol attesi: la somma delle probabilità di gol delle conclusioni */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal xg = BigDecimal.ZERO;

    @Column(nullable = false)
    private int falli;

    @Column(name = "calci_d_angolo", nullable = false)
    private int calciDAngolo;

    @Column(nullable = false)
    private int ammonizioni;

    @Column(nullable = false)
    private int espulsioni;
}
