package com.ilmister.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ilmister.entity.FormazioneGiocatore;

public interface FormazioneGiocatoreRepository extends JpaRepository<FormazioneGiocatore, Long> {

    List<FormazioneGiocatore> findByFormazioneIdOrderByPostoAsc(Long formazioneId);

    /**
     * Svuota la formazione prima di riscriverla. Si cancella con una query e non con orphanRemoval:
     * Hibernate eseguirebbe gli inserimenti prima delle cancellazioni e i vincoli di unicità sui posti
     * darebbero errore.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from FormazioneGiocatore f where f.formazione.id = :formazioneId")
    int eliminaPerFormazione(@Param("formazioneId") Long formazioneId);

    /** Il giocatore compare nella formazione salvata della squadra? */
    boolean existsByFormazioneSquadraIdAndGiocatoreId(Long squadraId, Long giocatoreId);
}
