package com.ilmister.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ilmister.entity.PrestazioneGiocatore;

public interface PrestazioneGiocatoreRepository extends JpaRepository<PrestazioneGiocatore, Long> {

    List<PrestazioneGiocatore> findByPartitaId(Long partitaId);
}
