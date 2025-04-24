package io.dodn.springboot.core.api.controller.v1.response

import io.dodn.springboot.core.domain.feed.Feed

data class FeedResponse(
    val feedId: Long,
    val worry: WorryResponse,
    val empathyCount: Long,
) {
    companion object {
        fun from(feed: Feed): FeedResponse {
            return FeedResponse(
                feedId = feed.id,
                worry = WorryResponse.from(feed.worry),
                empathyCount = feed.empathyCount,
            )
        }
    }
}
