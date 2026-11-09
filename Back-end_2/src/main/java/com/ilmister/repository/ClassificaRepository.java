package com.ilmister.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ilmister.entity.VoceClassifica;

public interface ClassificaRepository extends JpaRepository<VoceClassifica, Long> {

    /** La classifica in ordine: punti, differenza reti, gol fatti, poi il nome della squadra */
    @Query("""
            select v from VoceClassifica v
            where v.stagione.id = :stagioneId and v.campionato.id = :campionatoId
            order by v.punti desc, (v.golFatti - v.golSubiti) desc, v.golFatti desc, v.squadra.nome asc
            """)
    List<VoceClassifica> classifica(@Param("stagioneId") Long stagioneId, @Param("campionatoId") Long campionatoId);

    Optional<VoceClassifica> findByStagioneIdAndSquadraId(Long stagioneId, Long squadraId);
}
