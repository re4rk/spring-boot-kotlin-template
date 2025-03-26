package io.dodn.springboot.storage.db.core.user.email

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.Optional

interface EmailVerificationTokenRepository : JpaRepository<EmailVerificationTokenEntity, Long> {
    fun findByToken(token: String): Optional<EmailVerificationTokenEntity>

    @Modifying
    @Transactional
    @Query("DELETE FROM EmailVerificationTokenEntity t WHERE t.userId = :userId")
    fun deleteAllByUserId(@Param("userId") userId: Long)

    @Modifying
    @Transactional
    @Query("DELETE FROM EmailVerificationTokenEntity t WHERE t.expiresAt < :now")
    fun deleteAllExpiredTokens(@Param("now") now: LocalDateTime)
}
