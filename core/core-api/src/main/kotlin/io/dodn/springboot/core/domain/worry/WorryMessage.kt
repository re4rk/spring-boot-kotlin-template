package io.dodn.springboot.core.domain.worry

data class WorryMessage(
    val id: Long = 0,
    val role: StepRole,
    val content: String,
    val messageOrder: Int,
)
