package com.ilmister.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ilmister.entity.EventoPartita;

public interface EventoPartitaRepository extends JpaRepository<EventoPartita, Long> {

    List<EventoPartita> findByPartitaIdOrderByOrdineAsc(Long partitaId);
}
