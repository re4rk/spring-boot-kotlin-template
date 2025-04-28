package io.dodn.springboot.core.domain.worry.preference

data class WorryPreference(
    val id: Long,
    val userId: Long,
    val expressionStyle: ExpressionStyle,
    val comfortStyle: ComfortStyle,
)
