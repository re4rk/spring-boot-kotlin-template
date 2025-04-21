package io.dodn.springboot.core.api.controller.v1.response

import io.dodn.springboot.core.domain.worry.Feedback

data class FeedbackResponse(
    val feedback: String,
    val tone: String?,
    val tags: List<String>,
) {
    companion object {
        fun from(feedback: Feedback): FeedbackResponse {
            return FeedbackResponse(
                feedback = feedback.content,
                tone = feedback.tone,
                tags = feedback.tags,
            )
        }
    }
}
