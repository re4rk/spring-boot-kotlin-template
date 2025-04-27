package io.dodn.springboot.core.domain.worry

data class WorryMessage(
    val id: Long = 0,
    val role: MeesageRole,
    val content: String,
    val messageOrder: Int,
)
