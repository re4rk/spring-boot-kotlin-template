package io.dodn.springboot.core.api.controller.v1.request

import io.dodn.springboot.core.domain.worry.preference.ComfortStyle
import io.dodn.springboot.core.domain.worry.preference.ExpressionStyle

/**
 * WorryPreference 요청 모델
 */
data class WorryPreferenceRequest(
    val expressionStyle: ExpressionStyle,
    val comfortStyle: ComfortStyle,
)
