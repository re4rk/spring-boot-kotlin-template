package io.dodn.springboot.storage.db.core.feed

import io.dodn.springboot.storage.db.core.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import java.time.LocalDateTime

@Entity
class FeedEntity(
    val ownerId: Long = 0,

    val worryId: Long = 0,

    val emotion: String = "",

    val content: String = "",

    @Column(nullable = false)
    val sharedAt: LocalDateTime = LocalDateTime.now(),
) : BaseEntity()
