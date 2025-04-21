package io.dodn.springboot.core.domain.worry

data class AiFeedback(
    val id: Long = 0,
    val feedback: String,
    val tone: String? = null,
    val tags: List<String> = emptyList(),
)
