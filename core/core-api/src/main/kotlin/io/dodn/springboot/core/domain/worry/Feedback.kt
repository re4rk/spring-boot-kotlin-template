package io.dodn.springboot.core.domain.worry

data class Feedback(
    val id: Long = 0,
    val content: String,
    val tone: String? = null,
    val tags: List<String> = emptyList(),
)
