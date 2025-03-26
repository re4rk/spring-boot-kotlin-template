package io.dodn.springboot.storage.db.core.user.email

import io.dodn.springboot.storage.db.core.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "email_verification_tokens")
class EmailVerificationTokenEntity(
    @Column(nullable = false, unique = true)
    val token: String,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(nullable = false)
    val expiresAt: LocalDateTime,
) : BaseEntity()
