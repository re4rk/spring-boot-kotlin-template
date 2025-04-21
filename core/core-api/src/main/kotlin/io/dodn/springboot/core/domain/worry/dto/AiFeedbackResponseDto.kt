package io.dodn.springboot.core.domain.worry.dto

import io.dodn.springboot.core.domain.worry.AiFeedback

data class AiFeedbackResponseDto(
    val feedback: String,
    val tone: String?,
    val tags: List<String>,
) {
    companion object {
        fun from(aiFeedback: AiFeedback): AiFeedbackResponseDto {
            return AiFeedbackResponseDto(
                feedback = aiFeedback.feedback,
                tone = aiFeedback.tone,
                tags = aiFeedback.tags,
            )
        }
    }
}
