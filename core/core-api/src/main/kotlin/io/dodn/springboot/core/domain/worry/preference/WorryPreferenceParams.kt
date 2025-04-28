package io.dodn.springboot.core.domain.worry.preference

/**
 * 고민 선호도 생성/수정 매개변수
 */
data class WorryPreferenceParams(
    val expressionStyle: ExpressionStyle,
    val comfortStyle: ComfortStyle,
)
