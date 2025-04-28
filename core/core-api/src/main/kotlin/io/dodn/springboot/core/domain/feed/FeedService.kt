package io.dodn.springboot.core.domain.feed

import io.dodn.springboot.core.domain.feed.empathy.EmpathyCounter
import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.core.support.error.ErrorType
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FeedService(
    private val feedStorage: FeedStorage,
    private val empathyCounter: EmpathyCounter,
) {
    @Transactional
    fun createFeedByWorry(userDetails: UserDetails, worryId: Long): Feed {
        return feedStorage.shareWorry(worryId)
    }

    @Transactional
    fun deleteFeed(userDetails: UserDetails, feedId: Long) {
        val feed = feedStorage.getFeed(feedId)

        val isOwner = userDetails.username.toLong() == feed.ownerId
        val isAdmin = userDetails.authorities.any { it.authority == "ROLE_ADMIN" }

        if (!isOwner && !isAdmin) {
            throw CoreException(ErrorType.FEED_PERMISSION_DENIED)
        }

        feedStorage.deleteFeed(feedId)
    }

    @Transactional(readOnly = true)
    fun getFeeds(emotion: String? = null, tag: String? = null): List<Feed> {
        val feeds = feedStorage.getAllFeeds()

        return feeds.filter { feed ->
            (emotion == null || feed.emotion == emotion) &&
                (tag == null || feed.content.contains(tag))
        }
    }

    @Transactional(readOnly = true)
    fun getFeed(feedId: Long): Feed {
        return feedStorage.getFeed(feedId)
    }

    @Transactional(readOnly = true)
    fun getFeedByOwnerId(userDetails: UserDetails, ownerId: Long): List<Feed> {
        if (userDetails.username.toLong() != ownerId &&
            !userDetails.authorities.any { it.authority == "ROLE_ADMIN" }
        ) {
            throw CoreException(ErrorType.FEED_PERMISSION_DENIED)
        }

        return feedStorage.getFeedByOwnerId(ownerId)
    }

    @Transactional
    fun addEmpathy(feedId: Long, userId: Long): Long {
        val feed = feedStorage.getFeed(feedId)
        return empathyCounter.addEmpathy(feed.id, userId)
    }

    @Transactional
    fun removeEmpathy(feedId: Long, userId: Long): Long {
        return empathyCounter.removeEmpathy(feedId, userId)
    }
}
