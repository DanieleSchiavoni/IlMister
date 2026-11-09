package com.ilmister.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ilmister.entity.Partita;
import com.ilmister.entity.enumerazioni.StatoPartita;

public interface PartitaRepository extends JpaRepository<Partita, Long> {

    List<Partita> findByGiornataId(Long giornataId);

    /** Quante partite di una stagione hanno ancora lo stato indicato (ad esempio "da giocare") */
    long countByGiornataStagioneIdAndStato(Long stagioneId, StatoPartita stato);

    /** Tutte le partite di un campionato in una stagione, in ordine di giornata */
    @Query("""
            select p from Partita p
            join fetch p.giornata g
            join fetch p.squadraCasa
            join fetch p.squadraTrasferta
            where g.stagione.id = :stagioneId and g.campionato.id = :campionatoId
            order by g.numero asc, p.id asc
            """)
    List<Partita> calendario(@Param("stagioneId") Long stagioneId, @Param("campionatoId") Long campionatoId);

    /** Una partita, solo se appartiene alla carriera indicata */
    Optional<Partita> findByIdAndGiornataStagioneCarrieraId(Long id, Long carrieraId);

    /** Il numero della prima giornata di un campionato con partite non ancora concluse; vuoto se sono tutte concluse */
    @Query("""
            select min(p.giornata.numero) from Partita p
            where p.giornata.stagione.id = :stagioneId and p.giornata.campionato.id = :campionatoId
              and p.stato <> :stato
            """)
    Optional<Integer> prossimaGiornata(@Param("stagioneId") Long stagioneId, @Param("campionatoId") Long campionatoId,
            @Param("stato") StatoPartita stato);
}
