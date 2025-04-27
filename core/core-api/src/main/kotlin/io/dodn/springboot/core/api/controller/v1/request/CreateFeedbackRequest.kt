package io.dodn.springboot.core.api.controller.v1.request

import io.dodn.springboot.core.domain.worry.WorryMessage

data class AddWorryMessageRequest(
    val message: String,
)

data class CreateFeedbackResponse(
    val feedbackId: Long,
    val feedback: String,
) {
    companion object {
        fun from(request: WorryMessage): CreateFeedbackResponse {
            return CreateFeedbackResponse(
                feedbackId = request.id,
                feedback = request.content,
            )
        }
    }
}
