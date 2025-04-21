package io.dodn.springboot.core.domain.worry.dto

import io.dodn.springboot.core.domain.feed.Feed
import io.dodn.springboot.core.domain.worry.WorryMode

data class FeedSummaryResponseDto(
    val feedId: Long,
    val emotion: String,
    val tags: List<String>,
    val summary: String,
) {
    companion object {
        fun from(feed: Feed): FeedSummaryResponseDto {
            val summary = when (feed.worry.mode) {
                WorryMode.LETTER -> "${feed.worry.content.take(20)}... 고민에 AI가 답변했어요."
                WorryMode.CONVO -> "대화형 고민에 AI가 공감해주었어요."
            }

            return FeedSummaryResponseDto(
                feedId = feed.id,
                emotion = feed.worry.emotion,
                tags = feed.feedback.tags,
                summary = summary,
            )
        }
    }
}
