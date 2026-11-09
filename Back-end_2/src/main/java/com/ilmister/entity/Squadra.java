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

@Entity
@Table(name = "squadre",
        uniqueConstraints = @UniqueConstraint(name = "uq_squadre_nome", columnNames = { "carriera_id", "nome" }))
@Getter
@Setter
@NoArgsConstructor
public class Squadra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carriera_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Carriera carriera;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campionato_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Campionato campionato;

    @Column(nullable = false, length = 80)
    private String nome;

    @Column(name = "nome_breve", nullable = false, length = 30)
    private String nomeBreve;

    @Column(length = 60)
    private String citta;

    @Column(nullable = false, length = 80)
    private String stadio;

    @Column(nullable = false)
    private int capienza;

    /** Il prestigio della società, da 1 a 100: influenza incassi, mercato e fiducia */
    @Column(nullable = false)
    private int reputazione;

    @Column(name = "colore_primario", nullable = false, length = 7)
    private String colorePrimario = "#0d6efd";

    @Column(name = "colore_secondario", nullable = false, length = 7)
    private String coloreSecondario = "#ffffff";

    @Column(name = "budget_trasferimenti", nullable = false, precision = 14, scale = 2)
    private BigDecimal budgetTrasferimenti = BigDecimal.ZERO;

    /** Vero per la squadra guidata dall'utente: ce n'è una sola per carriera */
    @Column(name = "allenata_dall_utente", nullable = false)
    private boolean allenataDallUtente = false;
}
