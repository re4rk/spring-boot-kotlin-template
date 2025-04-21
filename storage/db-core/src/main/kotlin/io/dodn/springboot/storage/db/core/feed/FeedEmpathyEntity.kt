package io.dodn.springboot.storage.db.core.feed

import io.dodn.springboot.storage.db.core.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["feed_id", "user_id"]),
    ],
)
class FeedEmpathyEntity(
    @Column(name = "feed_id", insertable = false, updatable = false)
    val feedId: Long,

    @Column
    val userId: Long?,
) : BaseEntity()
