package com.ilmister.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ilmister.entity.Giornata;

public interface GiornataRepository extends JpaRepository<Giornata, Long> {

    List<Giornata> findByStagioneIdAndCampionatoIdOrderByNumeroAsc(Long stagioneId, Long campionatoId);

    Optional<Giornata> findByStagioneIdAndCampionatoIdAndNumero(Long stagioneId, Long campionatoId, int numero);
}
