package com.ilmister.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ilmister.entity.Utente;

public interface UtenteRepository extends JpaRepository<Utente, Long> {

    Optional<Utente> findByUsernameIgnoreCase(String username);

    Optional<Utente> findByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);
}
