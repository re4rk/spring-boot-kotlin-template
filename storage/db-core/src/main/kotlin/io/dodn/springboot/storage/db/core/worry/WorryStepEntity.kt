package io.dodn.springboot.storage.db.core.worry

import io.dodn.springboot.storage.db.core.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
class WorryStepEntity(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worry_id", nullable = false)
    val worry: WorryEntity? = null,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val role: StepRole,

    @Column(nullable = false, columnDefinition = "TEXT")
    val content: String,

    @Column(nullable = false)
    val stepOrder: Int,
) : BaseEntity()

