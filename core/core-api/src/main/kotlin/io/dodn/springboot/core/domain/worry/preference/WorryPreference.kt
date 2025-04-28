package io.dodn.springboot.core.domain.worry.preference

import java.time.LocalDateTime

data class WorryPreference(
    val id: Long,
    val userId: Long,
    val expressionStyle: ExpressionStyle,
    val comfortStyle: ComfortStyle,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
)
