package io.dodn.springboot.storage.db.core.worry

import io.dodn.springboot.storage.db.core.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
class WorryOptionEntity(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worry_id", nullable = false)
    val worry: WorryEntity? = null,

    @Column(nullable = false, length = 1)
    val label: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val text: String,
) : BaseEntity()
