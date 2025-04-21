package io.dodn.springboot.core.domain.worry.dto

data class CreateAiFeedbackRequestDto(
    val feedback: String,
    val tone: String? = null,
    val tags: List<String>? = null,
)
