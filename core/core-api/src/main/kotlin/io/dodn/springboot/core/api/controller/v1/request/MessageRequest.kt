package io.dodn.springboot.core.api.controller.v1.request

import io.dodn.springboot.core.domain.worry.MessageRole

data class MessageRequest(
    val role: MessageRole,
    val content: String,
)
