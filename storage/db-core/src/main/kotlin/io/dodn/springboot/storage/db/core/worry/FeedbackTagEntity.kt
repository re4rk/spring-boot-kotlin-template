package io.dodn.springboot.storage.db.core.worry

import io.dodn.springboot.storage.db.core.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
class FeedbackTagEntity(
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "feedback_id")
    val feedback: FeedbackEntity,

    @Column(nullable = false)
    val tag: String,
) : BaseEntity()
