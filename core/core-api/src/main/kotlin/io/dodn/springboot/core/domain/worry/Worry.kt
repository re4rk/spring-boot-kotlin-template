package io.dodn.springboot.core.domain.worry

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
