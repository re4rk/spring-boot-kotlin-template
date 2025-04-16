package io.dodn.springboot.storage.db.core.feed

import org.springframework.data.jpa.repository.JpaRepository

interface FeedRepository : JpaRepository<FeedEntity, Long> {
    fun findAllByOrderBySharedAtDesc(): List<FeedEntity>
    fun findByWorryId(worryId: Long): FeedEntity?
}
