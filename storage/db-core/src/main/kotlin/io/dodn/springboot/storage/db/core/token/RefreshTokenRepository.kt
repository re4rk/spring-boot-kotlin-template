package io.dodn.springboot.storage.db.core.token

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.Optional

interface RefreshTokenRepository : JpaRepository<RefreshTokenEntity, Long> {
    fun findByToken(token: String): Optional<RefreshTokenEntity>

    fun findByUserId(userId: Long): List<RefreshTokenEntity>

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshTokenEntity t WHERE t.userId = :userId")
    fun deleteAllByUserId(@Param("userId") userId: Long)

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshTokenEntity t WHERE t.expiresAt < :now")
    fun deleteAllExpiredTokens(@Param("now") now: LocalDateTime)
}