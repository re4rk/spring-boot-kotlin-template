package io.dodn.springboot.storage.db.core.feed

import io.dodn.springboot.storage.db.core.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import java.time.LocalDateTime

@Entity
class FeedEntity(
    val ownerId: Long = 0,

    val worryId: Long = 0,

    val feedbackId: Long = 0,

    @Column(nullable = false)
    val sharedAt: LocalDateTime = LocalDateTime.now(),
) : BaseEntity()
