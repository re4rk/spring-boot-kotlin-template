package io.dodn.springboot.storage.db.core.worry

import org.springframework.data.jpa.repository.JpaRepository

interface WorryStepRepository : JpaRepository<WorryStepEntity, Long> {
    fun findByWorryIdOrderByStepOrder(worryId: Long): List<WorryStepEntity>
}

interface WorryOptionRepository : JpaRepository<WorryOptionEntity, Long> {
    fun findByWorryId(worryId: Long): List<WorryOptionEntity>
}

interface AiFeedbackRepository : JpaRepository<AiFeedbackEntity, Long> {
    fun findByWorryId(worryId: Long): List<AiFeedbackEntity>
}

interface FeedbackTagRepository : JpaRepository<FeedbackTagEntity, Long> {
    fun findByFeedbackId(feedbackId: Long): List<FeedbackTagEntity>
}
