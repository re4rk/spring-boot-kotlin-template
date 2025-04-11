package io.dodn.springboot.storage.db.core.worry

import io.dodn.springboot.storage.db.core.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Entity
class WorryEntity(
    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val mode: WorryMode,

    @Column(nullable = false)
    val emotion: String,

    @Column(nullable = false)
    val category: String,

    @Column(columnDefinition = "TEXT")
    val content: String,

    @Column(nullable = false)
    var isShared: Boolean = false,
) : BaseEntity()
