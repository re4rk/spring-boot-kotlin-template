package io.dodn.springboot.core.api.controller.v1.response

import io.dodn.springboot.core.domain.feed.Feed

data class FeedResponseDto(
    val feedId: Long,
    val worry: WorryResponseDto,
    val feedback: FeedbackResponseDto,
    val empathyCount: Long,
) {
    companion object {
        fun from(feed: Feed): FeedResponseDto {
            return FeedResponseDto(
                feedId = feed.id,
                worry = WorryResponseDto.from(feed.worry),
                feedback = FeedbackResponseDto.from(feed.feedback),
                empathyCount = feed.empathyCount,
            )
        }
    }
}
