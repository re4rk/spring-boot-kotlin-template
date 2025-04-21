package io.dodn.springboot.core.domain.worry.dto

import io.dodn.springboot.core.domain.feed.Feed

data class FeedResponseDto(
    val feedId: Long,
    val worry: WorryResponseDto,
    val feedback: AiFeedbackResponseDto,
    val empathyCount: Long,
) {
    companion object {
        fun from(feed: Feed): FeedResponseDto {
            return FeedResponseDto(
                feedId = feed.id,
                worry = WorryResponseDto.from(feed.worry),
                feedback = AiFeedbackResponseDto.from(feed.feedback),
                empathyCount = feed.empathyCount,
            )
        }
    }
}
