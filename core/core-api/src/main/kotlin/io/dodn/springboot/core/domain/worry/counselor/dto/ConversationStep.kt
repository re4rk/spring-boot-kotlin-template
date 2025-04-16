package io.dodn.springboot.core.domain.worry.counselor.dto

data class ConversationStep(
    val role: String, // "user" or "ai"
    val content: String,
)
