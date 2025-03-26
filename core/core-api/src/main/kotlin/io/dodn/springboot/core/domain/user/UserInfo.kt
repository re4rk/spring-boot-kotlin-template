package io.dodn.springboot.core.domain.user

import io.dodn.springboot.storage.db.core.user.UserRole
import io.dodn.springboot.storage.db.core.user.UserStatus
import java.time.LocalDateTime

data class UserInfo(
    val id: Long,
    val email: String,
    val name: String?,
    val status: UserStatus,
    val role: UserRole,
    val lastLoginAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
