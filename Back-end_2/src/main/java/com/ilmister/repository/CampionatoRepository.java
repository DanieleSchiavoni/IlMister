package com.ilmister.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ilmister.entity.Campionato;

public interface CampionatoRepository extends JpaRepository<Campionato, Long> {

    List<Campionato> findByCarrieraIdOrderByLivelloAscNomeAsc(Long carrieraId);
}
