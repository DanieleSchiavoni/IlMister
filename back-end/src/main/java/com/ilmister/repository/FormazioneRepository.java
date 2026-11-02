package com.ilmister.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ilmister.entity.Formazione;

public interface FormazioneRepository extends JpaRepository<Formazione, Long> {

    Optional<Formazione> findBySquadraId(Long squadraId);
}
