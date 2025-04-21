package io.dodn.springboot.core.domain.feed

import io.dodn.springboot.core.domain.worry.WorryStorage
import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.core.support.error.ErrorType
import io.dodn.springboot.storage.db.core.feed.FeedEmpathyEntity
import io.dodn.springboot.storage.db.core.feed.FeedEmpathyRepository
import io.dodn.springboot.storage.db.core.feed.FeedEntity
import io.dodn.springboot.storage.db.core.feed.FeedRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class FeedStorage(
    private val feedRepository: FeedRepository,
    private val feedEmpathyRepository: FeedEmpathyRepository,
    private val worryStorage: WorryStorage,
) {
    @Transactional
    fun shareWorry(worryId: Long, feedbackId: Long): Feed {
        val feedEntity = feedRepository.save(FeedEntity(worryId = worryId, feedbackId = feedbackId))

        return mapToFeed(feedEntity)
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

    @Transactional
    fun saveEmpathy(feedId: Long, userId: Long?): Long {
        val feedEntity = feedRepository.findById(feedId)
            .orElseThrow { CoreException(ErrorType.DEFAULT_ERROR, "Feed not found") }

        if (userId != null && hasUserEmpathized(feedId, userId)) {
            throw CoreException(ErrorType.DEFAULT_ERROR, "Already liked")
        }

        feedEmpathyRepository.save(FeedEmpathyEntity(feed = feedEntity, userId = userId))

        return getEmpathyCount(feedId)
    }

    @Transactional(readOnly = true)
    fun getEmpathyCount(feedId: Long): Long {
        return feedEmpathyRepository.countByFeedId(feedId)
    }

    @Transactional(readOnly = true)
    fun hasUserEmpathized(feedId: Long, userId: Long): Boolean {
        return feedEmpathyRepository.findByFeedIdAndUserId(feedId, userId) != null
    }

    private fun mapToFeed(feedEntity: FeedEntity): Feed {
        val worry = worryStorage.getWorry(feedEntity.worryId)
        val feedback = worryStorage.getFeedback(feedEntity.feedbackId)
        val empathyCount = getEmpathyCount(feedEntity.id)

        return Feed(
            id = feedEntity.id,
            ownerId = feedEntity.ownerId,
            worry = worry,
            feedback = feedback,
            empathyCount = empathyCount,
            sharedAt = feedEntity.sharedAt,
        )
    }
}
