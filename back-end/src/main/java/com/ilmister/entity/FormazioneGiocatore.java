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

/** Un giocatore schierato: i posti da 0 a 10 sono i titolari, dall'11 in poi la panchina */
@Entity
@Table(name = "formazione_giocatori",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_formazione_giocatore", columnNames = { "formazione_id", "giocatore_id" }),
                @UniqueConstraint(name = "uq_formazione_posto", columnNames = { "formazione_id", "posto" }) })
@Getter
@Setter
@NoArgsConstructor
public class FormazioneGiocatore {

    public static final int PRIMO_POSTO_PANCHINA = 11;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "formazione_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Formazione formazione;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "giocatore_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Giocatore giocatore;

    @Column(nullable = false)
    private int posto;

    public boolean titolare() {
        return posto < PRIMO_POSTO_PANCHINA;
    }
}
