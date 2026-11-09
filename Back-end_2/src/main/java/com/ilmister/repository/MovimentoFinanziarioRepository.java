package com.ilmister.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ilmister.entity.MovimentoFinanziario;

public interface MovimentoFinanziarioRepository extends JpaRepository<MovimentoFinanziario, Long> {

    /** Gli ultimi 30 movimenti di una squadra, i più recenti per primi */
    List<MovimentoFinanziario> findTop30BySquadraIdOrderByDataMovimentoDescIdDesc(Long squadraId);

    long countBySquadraId(Long squadraId);

    @Query("select sum(m.importo) from MovimentoFinanziario m where m.squadra.id = :squadraId and m.importo > 0")
    Optional<BigDecimal> totaleEntrate(@Param("squadraId") Long squadraId);

    @Query("select sum(m.importo) from MovimentoFinanziario m where m.squadra.id = :squadraId and m.importo < 0")
    Optional<BigDecimal> totaleUscite(@Param("squadraId") Long squadraId);
}
