package io.dodn.springboot.core.api.controller.v1.request

import io.dodn.springboot.core.domain.worry.WorryStep

data class CreateFeedbackRequest(
    val feedback: String,
    val tone: String? = null,
    val tags: List<String>? = null,
)

data class AddWorryMessageRequest(
    val message: String,
)

data class CreateFeedbackResponse(
    val feedbackId: Long,
    val feedback: String,
) {
    companion object {
        fun from(request: WorryStep): CreateFeedbackResponse {
            return CreateFeedbackResponse(
                feedbackId = request.id,
                feedback = request.content,
            )
        }
    }
}
