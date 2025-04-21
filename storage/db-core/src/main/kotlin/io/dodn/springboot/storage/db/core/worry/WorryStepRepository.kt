package io.dodn.springboot.storage.db.core.worry

import org.springframework.data.jpa.repository.JpaRepository

interface WorryStepRepository : JpaRepository<WorryStepEntity, Long> {
    fun findByWorryIdOrderByStepOrder(worryId: Long): List<WorryStepEntity>
}

interface WorryOptionRepository : JpaRepository<WorryOptionEntity, Long> {
    fun findByWorryId(worryId: Long): List<WorryOptionEntity>
}

interface FeedbackRepository : JpaRepository<FeedbackEntity, Long> {
    fun findByWorryId(worryId: Long): List<FeedbackEntity>
}

interface FeedbackTagRepository : JpaRepository<FeedbackTagEntity, Long> {
    fun findByFeedbackId(feedbackId: Long): List<FeedbackTagEntity>
}
