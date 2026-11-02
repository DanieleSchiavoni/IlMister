package com.ilmister.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ilmister.entity.Partita;
import com.ilmister.entity.enumerazioni.StatoPartita;

public interface PartitaRepository extends JpaRepository<Partita, Long> {

    List<Partita> findByGiornataId(Long giornataId);

    /** Quante partite di una stagione hanno ancora lo stato indicato (ad esempio "da giocare") */
    long countByGiornataStagioneIdAndStato(Long stagioneId, StatoPartita stato);
}
