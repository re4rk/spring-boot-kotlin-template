package io.dodn.springboot.core.domain.worry

data class FeedbackDto(
    val id: Long = 0,
    val content: String,
    val tone: String? = null,
    val tags: List<String> = emptyList(),
) {
    companion object {
        fun from(feedback: Feedback): FeedbackDto = FeedbackDto(
            id = feedback.id,
            content = feedback.content,
            tone = feedback.tone,
            tags = feedback.tags,
        )
    }
}
