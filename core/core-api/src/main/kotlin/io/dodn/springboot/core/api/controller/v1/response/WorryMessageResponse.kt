package io.dodn.springboot.core.api.controller.v1.response

import io.dodn.springboot.core.domain.worry.WorryMessage

data class WorryMessageResponse(
    val role: String,
    val content: String,
) {
    companion object {
        fun from(message: WorryMessage): WorryMessageResponse {
            return WorryMessageResponse(
                role = message.role.name.lowercase(),
                content = message.content,
            )
        }
    }
}
