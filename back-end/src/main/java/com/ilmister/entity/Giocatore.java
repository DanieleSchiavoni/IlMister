package com.ilmister.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import com.ilmister.entity.enumerazioni.Piede;
import com.ilmister.entity.enumerazioni.Posizione;
import com.ilmister.entity.enumerazioni.Ruolo;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Un calciatore. Gli attributi vanno da 1 a 100. Senza squadra (squadra nulla) è svincolato. */
@Entity
@Table(name = "giocatori")
@Getter
@Setter
@NoArgsConstructor
public class Giocatore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carriera_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Carriera carriera;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "squadra_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Squadra squadra;

    @Column(nullable = false, length = 60)
    private String nome;

    @Column(nullable = false, length = 60)
    private String cognome;

    @Column(name = "data_nascita", nullable = false)
    private LocalDate dataNascita;

    @Column(nullable = false, length = 40)
    private String nazionalita = "Italia";

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private Posizione posizione;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 15)
    private Piede piede = Piede.DESTRO;

    @Column(name = "numero_maglia")
    private Integer numeroMaglia;

    // Attributi tecnici e fisici
    @Column(nullable = false)
    private int velocita;
    @Column(nullable = false)
    private int resistenza;
    @Column(nullable = false)
    private int forza;
    @Column(nullable = false)
    private int tecnica;
    @Column(nullable = false)
    private int passaggio;
    @Column(nullable = false)
    private int tiro;
    @Column(nullable = false)
    private int dribbling;
    @Column(nullable = false)
    private int contrasto;
    @Column(nullable = false)
    private int intelligenza;
    @Column(nullable = false)
    private int parate;

    /** Il livello complessivo attuale e quello che può raggiungere */
    @Column(nullable = false)
    private int valutazione;
    @Column(nullable = false)
    private int potenziale;

    // Stato del momento
    @Column(nullable = false)
    private int forma = 50;
    @Column(nullable = false)
    private int morale = 70;
    @Column(nullable = false)
    private int energia = 100;

    /** Ultimo giorno di indisponibilità per infortunio (compreso); nullo se in salute */
    @Column(name = "infortunato_fino_al")
    private LocalDate infortunatoFinoAl;

    @Column(name = "tipo_infortunio", length = 60)
    private String tipoInfortunio;

    @Column(name = "squalifica_giornate", nullable = false)
    private int squalificaGiornate = 0;

    // Contratto
    @Column(name = "stipendio_annuo", nullable = false, precision = 12, scale = 2)
    private BigDecimal stipendioAnnuo;

    @Column(name = "scadenza_contratto", nullable = false)
    private LocalDate scadenzaContratto;

    @Column(name = "valore_mercato", nullable = false, precision = 14, scale = 2)
    private BigDecimal valoreMercato;

    /** Il reparto, ricavato dalla posizione */
    public Ruolo getRuolo() {
        return posizione.getRuolo();
    }

    public int etaAl(LocalDate data) {
        return Period.between(dataNascita, data).getYears();
    }

    public boolean infortunatoAl(LocalDate data) {
        return infortunatoFinoAl != null && !data.isAfter(infortunatoFinoAl);
    }

    /** Può giocare: né infortunato né squalificato */
    public boolean disponibileAl(LocalDate data) {
        return !infortunatoAl(data) && squalificaGiornate == 0;
    }

    public String nomeCompleto() {
        return nome + " " + cognome;
    }
}
