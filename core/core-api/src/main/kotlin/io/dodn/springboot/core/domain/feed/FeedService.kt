package io.dodn.springboot.core.domain.feed

import io.dodn.springboot.core.domain.worry.WorryStorage
import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.core.support.error.ErrorType
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FeedService(
    private val feedStorage: FeedStorage,
    private val worryStorage: WorryStorage,
) {
    @Transactional
    fun createFeedByWorry(userDetails: UserDetails, worryId: Long, feedbackId: Long): Feed {
        val worry = worryStorage.getWorry(worryId)

        val isOwner = userDetails.username.toLong() == worry.userId

        if (!isOwner) {
            throw CoreException(ErrorType.FEED_PERMISSION_DENIED)
        }

        return feedStorage.shareWorry(worryId, feedbackId)
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
            (emotion == null || feed.worry.emotion == emotion) &&
                (tag == null || feed.feedback.tags.contains(tag))
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
    fun addEmpathy(feedId: Long, userId: Long?): Long {
        if (userId != null && feedStorage.hasUserEmpathized(feedId, userId)) {
            throw CoreException(ErrorType.FEED_ALREADY_EMPATHIZED)
        }

        return feedStorage.saveEmpathy(feedId, userId)
    }
}
