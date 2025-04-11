package io.dodn.springboot.storage.db.core.worry

import io.dodn.springboot.storage.db.core.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import java.time.LocalDateTime

@Entity
class FeedEntity(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worry_id")
    val worry: WorryEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feedback_id")
    val feedback: AiFeedbackEntity,

    @Column(nullable = false)
    val sharedAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "feed")
    val empathies: MutableList<FeedEmpathyEntity> = mutableListOf(),
) : BaseEntity()
