package com.ilmister.entity;

import java.time.LocalDate;

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
@Table(name = "giornate",
        uniqueConstraints = @UniqueConstraint(name = "uq_giornate_numero",
                columnNames = { "stagione_id", "campionato_id", "numero" }))
@Getter
@Setter
@NoArgsConstructor
public class Giornata {

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

    @Column(nullable = false)
    private int numero;

    @Column(name = "data_giornata", nullable = false)
    private LocalDate dataGiornata;
}
