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

/** Un campionato; il livello 1 è il più alto. In futuro più livelli e gironi (promozioni e retrocessioni) */
@Entity
@Table(name = "campionati",
        uniqueConstraints = @UniqueConstraint(name = "uq_campionati_nome", columnNames = { "carriera_id", "nome" }))
@Getter
@Setter
@NoArgsConstructor
public class Campionato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carriera_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Carriera carriera;

    @Column(nullable = false, length = 80)
    private String nome;

    @Column(nullable = false)
    private int livello = 1;

    @Column(length = 10)
    private String girone;
}
