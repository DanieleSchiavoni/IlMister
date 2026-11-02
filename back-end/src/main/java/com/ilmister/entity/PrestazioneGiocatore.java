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

/** Ciò che un giocatore ha fatto in una partita: minuti, gol, assist, voto */
@Entity
@Table(name = "prestazioni_giocatori",
        uniqueConstraints = @UniqueConstraint(name = "uq_prestazioni_giocatore", columnNames = { "partita_id", "giocatore_id" }))
@Getter
@Setter
@NoArgsConstructor
public class PrestazioneGiocatore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partita_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Partita partita;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "giocatore_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Giocatore giocatore;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "squadra_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Squadra squadra;

    @Column(nullable = false)
    private boolean titolare;

    @Column(name = "minuti_giocati", nullable = false)
    private int minutiGiocati = 0;

    @Column(nullable = false)
    private int gol = 0;

    @Column(nullable = false)
    private int assist = 0;

    @Column(nullable = false)
    private int tiri = 0;

    @Column(nullable = false)
    private boolean ammonito = false;

    @Column(nullable = false)
    private boolean espulso = false;

    /** Il voto, da 1,0 a 10,0 */
    @Column(nullable = false, precision = 3, scale = 1)
    private BigDecimal voto;
}
