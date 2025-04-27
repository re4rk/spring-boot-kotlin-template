package io.dodn.springboot.core.domain.worry.counselor.dto

data class CounselingRequest(
    val userInput: String,
    val emotion: String,
    val category: String,
    val options: List<String> = emptyList(),
    val messages: List<ConversationMessage> = emptyList(),
)
