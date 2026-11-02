package com.ilmister.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import com.ilmister.entity.enumerazioni.LineaDifensiva;
import com.ilmister.entity.enumerazioni.Mentalita;
import com.ilmister.entity.enumerazioni.Modulo;
import com.ilmister.entity.enumerazioni.Pressing;
import com.ilmister.entity.enumerazioni.Ritmo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Il modulo e le istruzioni tattiche di una squadra; i giocatori schierati sono in FormazioneGiocatore */
@Entity
@Table(name = "formazioni")
@Getter
@Setter
@NoArgsConstructor
public class Formazione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "squadra_id", nullable = false, unique = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Squadra squadra;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private Modulo modulo;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private Mentalita mentalita = Mentalita.EQUILIBRATA;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 10)
    private Pressing pressing = Pressing.MEDIO;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 10)
    private Ritmo ritmo = Ritmo.NORMALE;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "linea_difensiva", nullable = false, length = 10)
    private LineaDifensiva lineaDifensiva = LineaDifensiva.MEDIA;
}
