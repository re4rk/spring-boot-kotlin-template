package io.dodn.springboot.core.api.controller.v1.response

import io.dodn.springboot.core.domain.worry.WorryMessage

data class WorryStepResponse(
    val role: String,
    val content: String,
) {
    companion object {
        fun from(step: WorryMessage): WorryStepResponse {
            return WorryStepResponse(
                role = step.role.name.lowercase(),
                content = step.content,
            )
        }
    }
}
