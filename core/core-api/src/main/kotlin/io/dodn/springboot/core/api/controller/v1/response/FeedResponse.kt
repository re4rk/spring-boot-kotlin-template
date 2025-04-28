package io.dodn.springboot.core.api.controller.v1.response

import io.dodn.springboot.core.domain.feed.Feed

data class FeedResponse(
    val feedId: Long,
    val emotion: String,
    val content: String,
    val empathyCount: Long,
) {
    companion object {
        fun from(feed: Feed): FeedResponse {
            return FeedResponse(
                feedId = feed.id,
                emotion = feed.emotion,
                content = feed.content,
                empathyCount = feed.empathyCount,
            )
        }
    }
}
