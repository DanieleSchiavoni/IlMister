package com.ilmister.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ilmister.entity.Carriera;

public interface CarrieraRepository extends JpaRepository<Carriera, Long> {

    List<Carriera> findByUtenteIdOrderByDataAggiornamentoDesc(Long utenteId);

    /** L'unico modo di leggere una carriera dall'esterno: chi non ne è il proprietario non la trova */
    Optional<Carriera> findByIdAndUtenteId(Long id, Long utenteId);

    long countByUtenteId(Long utenteId);
}
