package io.dodn.springboot.core.domain.feed

import io.dodn.springboot.core.domain.worry.WorryStorage
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FeedService(
    private val feedStorage: FeedStorage,
    private val worryStorage: WorryStorage,
) {
    @Transactional
    fun shareWorry(worryId: Long, aiFeedbackId: Long): Feed {
        worryStorage.updateWorrySharedStatus(worryId, true)

        return feedStorage.saveFeed(worryId, aiFeedbackId)
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

    @Transactional
    fun addEmpathy(feedId: Long, userId: Long?): Long {
        if (userId != null && feedStorage.hasUserEmpathized(feedId, userId)) {
            throw IllegalStateException("User has already empathized with this feed")
        }

        return feedStorage.saveEmpathy(feedId, userId)
    }
}
