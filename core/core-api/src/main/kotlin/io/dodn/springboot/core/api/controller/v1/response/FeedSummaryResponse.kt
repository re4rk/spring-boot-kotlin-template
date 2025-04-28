package io.dodn.springboot.core.api.controller.v1.response

import io.dodn.springboot.core.domain.feed.Feed

data class FeedSummaryResponse(
    val feedId: Long,
    val emotion: String,
    val tags: List<String>,
    val summary: String,
) {
    companion object {
        fun from(feed: Feed): FeedSummaryResponse {
            return FeedSummaryResponse(
                feedId = feed.id,
                emotion = feed.emotion,
                tags = ArrayList(),
                summary = feed.content,
            )
        }
    }
}
