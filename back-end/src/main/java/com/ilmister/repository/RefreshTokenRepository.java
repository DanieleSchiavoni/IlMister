package com.ilmister.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ilmister.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByHashToken(String hashToken);

    /** Chiude tutte le sessioni di un utente (cambio password, blocco, eliminazione) */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from RefreshToken t where t.utente.id = :utenteId")
    int eliminaPerUtente(@Param("utenteId") Long utenteId);

    /** Pulizia periodica dei token scaduti */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from RefreshToken t where t.scadenza < :limite")
    int eliminaScaduti(@Param("limite") LocalDateTime limite);
}
