package io.dodn.springboot.core.domain.worry

data class WorryStep(
    val id: Long = 0,
    val role: StepRole,
    val content: String,
    val stepOrder: Int,
)
