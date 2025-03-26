package io.dodn.springboot.storage.db.core.user.password

import io.dodn.springboot.storage.db.core.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "password_history")
class PasswordHistoryEntity(
    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(nullable = false)
    val password: String,
) : BaseEntity()
