package com.ilmister.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ilmister.entity.Giocatore;

public interface GiocatoreRepository extends JpaRepository<Giocatore, Long> {

    List<Giocatore> findBySquadraIdOrderByCognomeAscNomeAsc(Long squadraId);

    Optional<Giocatore> findByIdAndCarrieraId(Long id, Long carrieraId);

    /** I giocatori svincolati di una carriera, cioè senza squadra */
    List<Giocatore> findByCarrieraIdAndSquadraIsNull(Long carrieraId);

    long countBySquadraId(Long squadraId);
}
