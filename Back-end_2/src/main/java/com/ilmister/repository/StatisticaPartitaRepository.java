package com.ilmister.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ilmister.entity.StatisticaPartita;

public interface StatisticaPartitaRepository extends JpaRepository<StatisticaPartita, Long> {

    List<StatisticaPartita> findByPartitaId(Long partitaId);
}
