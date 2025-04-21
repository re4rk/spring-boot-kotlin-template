package io.dodn.springboot.storage.db.core.feed

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock

interface FeedEmpathyRepository : JpaRepository<FeedEmpathyEntity, Long> {
    fun countByFeedId(feedId: Long): Long

    @Lock(LockModeType.OPTIMISTIC)
    fun findByFeedIdAndOwnerId(feedId: Long, userId: Long): FeedEmpathyEntity?
}
