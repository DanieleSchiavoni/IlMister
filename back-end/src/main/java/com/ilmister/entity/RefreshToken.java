package com.ilmister.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Un token di rinnovo della sessione: nel database c'è solo l'impronta (hash SHA-256), mai il token */
@Entity
@Table(name = "refresh_token")
@Getter
@Setter
@NoArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utente_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Utente utente;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "hash_token", nullable = false, unique = true, length = 64)
    private String hashToken;

    @Column(nullable = false)
    private LocalDateTime scadenza;

    @Column(nullable = false)
    private boolean revocato = false;

    @Column(name = "data_creazione", nullable = false, updatable = false)
    private LocalDateTime dataCreazione;

    @PrePersist
    void allaCreazione() {
        dataCreazione = LocalDateTime.now();
    }
}
