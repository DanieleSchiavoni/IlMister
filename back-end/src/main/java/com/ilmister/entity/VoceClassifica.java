package com.ilmister.entity;

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

/** La riga di una squadra nella classifica di una stagione */
@Entity
@Table(name = "classifica",
        uniqueConstraints = @UniqueConstraint(name = "uq_classifica_squadra", columnNames = { "stagione_id", "squadra_id" }))
@Getter
@Setter
@NoArgsConstructor
public class VoceClassifica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stagione_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Stagione stagione;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campionato_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Campionato campionato;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "squadra_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Squadra squadra;

    @Column(nullable = false)
    private int giocate = 0;
    @Column(nullable = false)
    private int vittorie = 0;
    @Column(nullable = false)
    private int pareggi = 0;
    @Column(nullable = false)
    private int sconfitte = 0;
    @Column(name = "gol_fatti", nullable = false)
    private int golFatti = 0;
    @Column(name = "gol_subiti", nullable = false)
    private int golSubiti = 0;
    @Column(nullable = false)
    private int punti = 0;

    public int differenzaReti() {
        return golFatti - golSubiti;
    }
}
