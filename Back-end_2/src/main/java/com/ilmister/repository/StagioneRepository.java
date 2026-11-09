package com.ilmister.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ilmister.entity.Stagione;
import com.ilmister.entity.enumerazioni.StatoStagione;

public interface StagioneRepository extends JpaRepository<Stagione, Long> {

    Optional<Stagione> findByCarrieraIdAndStato(Long carrieraId, StatoStagione stato);

    List<Stagione> findByCarrieraIdOrderByAnnoInizioDesc(Long carrieraId);
}
