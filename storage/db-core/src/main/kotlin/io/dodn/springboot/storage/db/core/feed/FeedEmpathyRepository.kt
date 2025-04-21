package io.dodn.springboot.storage.db.core.feed

import org.springframework.data.jpa.repository.JpaRepository

interface FeedEmpathyRepository : JpaRepository<FeedEmpathyEntity, Long> {
    fun countByFeedId(feedId: Long): Long
    fun findByFeedIdAndUserId(feedId: Long, userId: Long): FeedEmpathyEntity?
}
