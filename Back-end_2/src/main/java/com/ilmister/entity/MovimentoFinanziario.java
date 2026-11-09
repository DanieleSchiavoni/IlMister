package com.ilmister.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import com.ilmister.entity.enumerazioni.TipoMovimento;

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

/** Un'entrata (importo positivo) o un'uscita (importo negativo) nel bilancio della squadra dell'utente */
@Entity
@Table(name = "movimenti_finanziari")
@Getter
@Setter
@NoArgsConstructor
public class MovimentoFinanziario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carriera_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Carriera carriera;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "squadra_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Squadra squadra;

    @Column(name = "data_movimento", nullable = false)
    private LocalDate dataMovimento;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private TipoMovimento tipo;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal importo;

    @Column(nullable = false, length = 120)
    private String descrizione;
}
