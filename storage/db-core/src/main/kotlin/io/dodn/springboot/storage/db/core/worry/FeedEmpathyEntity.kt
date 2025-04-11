package io.dodn.springboot.storage.db.core.worry

import io.dodn.springboot.storage.db.core.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.util.UUID

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["feed_id", "user_id"]),
    ],
)
class FeedEmpathyEntity(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id")
    val feed: FeedEntity,

    @Column
    val userId: Long?,
) : BaseEntity()
