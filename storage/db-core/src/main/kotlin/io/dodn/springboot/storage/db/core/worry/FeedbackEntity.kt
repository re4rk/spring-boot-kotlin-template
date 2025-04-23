package io.dodn.springboot.storage.db.core.worry

import io.dodn.springboot.storage.db.core.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany

@Entity
class FeedbackEntity(
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "worry_id")
    val worry: WorryEntity,

    @Column(nullable = false, columnDefinition = "TEXT")
    val feedback: String,

    @Column
    val tone: String?,

    @OneToMany(mappedBy = "feedback")
    val tags: List<FeedbackTagEntity> = mutableListOf(),
) : BaseEntity()
