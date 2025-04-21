package io.dodn.springboot.core.domain.feed.empathy

import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.core.support.error.ErrorType
import io.dodn.springboot.storage.db.core.feed.FeedEmpathyEntity
import io.dodn.springboot.storage.db.core.feed.FeedEmpathyRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class EmpathyCounter(
    private val feedEmpathyRepository: FeedEmpathyRepository,
) {
    @Transactional
    fun addEmpathy(feedId: Long, userId: Long): Long {
        if (feedEmpathyRepository.findByFeedIdAndOwnerId(feedId, userId) != null) {
            throw CoreException(ErrorType.DEFAULT_ERROR, "Already liked")
        }

        feedEmpathyRepository.save(FeedEmpathyEntity(feedId = feedId, ownerId = userId))

        return getEmpathyCount(feedId)
    }

    @Transactional
    fun removeEmpathy(feedId: Long, userId: Long): Long {
        val empathyEntity = feedEmpathyRepository.findByFeedIdAndOwnerId(feedId, userId)
            ?: throw CoreException(ErrorType.DEFAULT_ERROR, "Like not found")

        feedEmpathyRepository.delete(empathyEntity)

        return getEmpathyCount(feedId)
    }

    @Transactional(readOnly = true)
    fun getEmpathyCount(feedId: Long): Long {
        return feedEmpathyRepository.countByFeedId(feedId)
    }
}
