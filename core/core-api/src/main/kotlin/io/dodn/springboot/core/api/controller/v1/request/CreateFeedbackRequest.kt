package io.dodn.springboot.core.api.controller.v1.request

data class CreateFeedbackRequest(
    val feedback: String,
    val tone: String? = null,
    val tags: List<String>? = null,
)
