package io.dodn.springboot.core.domain.feed

import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.core.support.error.ErrorType
import io.dodn.springboot.storage.db.core.feed.FeedEmpathyEntity
import io.dodn.springboot.storage.db.core.feed.FeedEmpathyRepository
import io.dodn.springboot.storage.db.core.feed.FeedRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class EmpathyCounter(
    private val feedRepository: FeedRepository,
    private val feedEmpathyRepository: FeedEmpathyRepository,
) {
    @Transactional
    fun addEmpathy(feedId: Long, userId: Long?): Long {
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

    private fun hasUserEmpathized(feedId: Long, userId: Long): Boolean {
        return feedEmpathyRepository.findByFeedIdAndUserId(feedId, userId) != null
    }
}
