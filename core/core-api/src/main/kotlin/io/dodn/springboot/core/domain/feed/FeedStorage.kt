package io.dodn.springboot.core.domain.feed

import io.dodn.springboot.core.domain.feed.empathy.EmpathyCounter
import io.dodn.springboot.core.domain.worry.WorryStorage
import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.core.support.error.ErrorType
import io.dodn.springboot.storage.db.core.feed.FeedEntity
import io.dodn.springboot.storage.db.core.feed.FeedRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class FeedStorage(
    private val feedRepository: FeedRepository,
    private val worryStorage: WorryStorage,
    private val empathyCounter: EmpathyCounter,
) {
    @Transactional
    fun shareWorry(worryId: Long): Feed {
        val worry = worryStorage.getWorry(worryId)
        val feedEntity = FeedEntity(
            ownerId = worry.userId,
            worryId = worryId,
            emotion = worry.emotion,
            content = worry.content,
        )

        val savedFeedEntity = feedRepository.save(feedEntity)
        return mapToFeed(savedFeedEntity)
    }

    @Transactional
    fun deleteFeed(feedId: Long) {
        val feedEntity = feedRepository.findById(feedId)
            .orElseThrow { CoreException(ErrorType.DEFAULT_ERROR, "Feed not found") }

        feedRepository.delete(feedEntity)
    }

    @Transactional(readOnly = true)
    fun getFeed(feedId: Long): Feed {
        val feedEntity = feedRepository.findById(feedId)
            .orElseThrow { CoreException(ErrorType.DEFAULT_ERROR, "Feed not found") }

        return mapToFeed(feedEntity)
    }

    @Transactional(readOnly = true)
    fun getFeedByOwnerId(ownerId: Long): List<Feed> {
        return feedRepository.findByOwnerId(ownerId).map { mapToFeed(it) }
    }

    @Transactional(readOnly = true)
    fun getAllFeeds(): List<Feed> {
        return feedRepository.findAllByOrderBySharedAtDesc().map { mapToFeed(it) }
    }

    private fun mapToFeed(feedEntity: FeedEntity): Feed {
        return Feed(
            id = feedEntity.id,
            ownerId = feedEntity.ownerId,
            emotion = feedEntity.emotion,
            content = feedEntity.content,
            empathyCount = empathyCounter.getEmpathyCount(feedEntity.id),
            sharedAt = feedEntity.sharedAt,
        )
    }
}
