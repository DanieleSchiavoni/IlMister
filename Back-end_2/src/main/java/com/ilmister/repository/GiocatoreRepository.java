package com.ilmister.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ilmister.entity.Giocatore;

public interface GiocatoreRepository extends JpaRepository<Giocatore, Long> {

    List<Giocatore> findBySquadraIdOrderByCognomeAscNomeAsc(Long squadraId);

    Optional<Giocatore> findByIdAndCarrieraId(Long id, Long carrieraId);

    /** I giocatori svincolati di una carriera, cioè senza squadra */
    List<Giocatore> findByCarrieraIdAndSquadraIsNull(Long carrieraId);

    long countBySquadraId(Long squadraId);

    /** Tutti i giocatori delle squadre di un campionato */
    List<Giocatore> findBySquadraCampionatoId(Long campionatoId);

    /** I giocatori di tutte le altre squadre della carriera (esclusi gli svincolati), i più forti per primi */
    @Query("""
            select g from Giocatore g join fetch g.squadra
            where g.carriera.id = :carrieraId and g.squadra.id <> :squadraId
            order by g.valutazione desc, g.id asc
            """)
    List<Giocatore> dellAltreSquadre(@Param("carrieraId") Long carrieraId, @Param("squadraId") Long squadraId);
}
