package io.dodn.springboot.core.domain.worry

import java.util.UUID

enum class WorryMode {
    LETTER,
    CONVO,
}

data class Worry(
    val id: Long,
    val userId: Long,
    val mode: WorryMode,
    val emotion: String,
    val category: String,
    val content: String,
    val isShared: Boolean = false,
    val steps: List<WorryStep> = emptyList(),
    val options: List<WorryOption> = emptyList(),
)

enum class StepRole {
    USER,
    AI,
}

data class WorryStep(
    val id: Long = 0,
    val role: StepRole,
    val content: String,
    val stepOrder: Int,
)

data class WorryOption(
    val id: Long = 0,
    val label: String,
    val text: String,
)

data class AiFeedback(
    val id: Long = 0,
    val feedback: String,
    val tone: String? = null,
    val tags: List<String> = emptyList(),
)

data class Feed(
    val id: Long = 0,
    val worry: Worry,
    val feedback: AiFeedback,
    val empathyCount: Long = 0,
)
