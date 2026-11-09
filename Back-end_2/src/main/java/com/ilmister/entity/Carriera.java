package com.ilmister.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import com.ilmister.entity.enumerazioni.FocusAllenamento;
import com.ilmister.entity.enumerazioni.StatoCarriera;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Un salvataggio di gioco: tutti i dati di squadre, giocatori e partite appartengono a una carriera */
@Entity
@Table(name = "carriere")
@Getter
@Setter
@NoArgsConstructor
public class Carriera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utente_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Utente utente;

    @Column(nullable = false, length = 80)
    private String nome;

    @Column(name = "nome_allenatore", nullable = false, length = 80)
    private String nomeAllenatore;

    /** La data "di oggi" nel gioco: avanza giornata dopo giornata */
    @Column(name = "data_corrente", nullable = false)
    private LocalDate dataCorrente;

    /** La fiducia del presidente nell'allenatore, da 0 a 100 */
    @Column(name = "fiducia_presidente", nullable = false)
    private int fiduciaPresidente = 60;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private StatoCarriera stato = StatoCarriera.IN_CORSO;

    /** Su cosa si allena la squadra dell'utente */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "focus_allenamento", nullable = false, length = 20)
    private FocusAllenamento focusAllenamento = FocusAllenamento.EQUILIBRATO;

    /** Da questo seme nascono tutti i numeri casuali della carriera: a parità di seme il gioco è ripetibile */
    @Column(nullable = false)
    private long seme;

    /** Impedisce che due richieste insieme (ad esempio un doppio clic su "Avanti") modifichino la carriera */
    @Version
    @Column(nullable = false)
    private Long versione;

    @Column(name = "data_creazione", nullable = false, updatable = false)
    private LocalDateTime dataCreazione;

    @Column(name = "data_aggiornamento", nullable = false)
    private LocalDateTime dataAggiornamento;

    @PrePersist
    void allaCreazione() {
        LocalDateTime ora = LocalDateTime.now();
        dataCreazione = ora;
        dataAggiornamento = ora;
    }

    @PreUpdate
    void alloAggiornamento() {
        dataAggiornamento = LocalDateTime.now();
    }
}
