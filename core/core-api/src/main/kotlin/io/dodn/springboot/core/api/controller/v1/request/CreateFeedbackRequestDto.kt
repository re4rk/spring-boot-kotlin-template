package io.dodn.springboot.core.api.controller.v1.request

data class CreateFeedbackRequestDto(
    val feedback: String,
    val tone: String? = null,
    val tags: List<String>? = null,
)
