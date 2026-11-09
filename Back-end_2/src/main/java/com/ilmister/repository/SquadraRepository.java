package com.ilmister.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ilmister.entity.Squadra;

public interface SquadraRepository extends JpaRepository<Squadra, Long> {

    List<Squadra> findByCampionatoIdOrderByNomeAsc(Long campionatoId);

    List<Squadra> findByCarrieraId(Long carrieraId);

    Optional<Squadra> findByIdAndCarrieraId(Long id, Long carrieraId);

    /** La squadra guidata dall'utente in una carriera */
    Optional<Squadra> findByCarrieraIdAndAllenataDallUtenteTrue(Long carrieraId);
}
