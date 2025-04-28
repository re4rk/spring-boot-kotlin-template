package io.dodn.springboot.core.api.controller.v1.response

/**
 * WorryPreference 옵션 응답 모델
 */
data class WorryPreferenceOptionsResponse(
    val expressionStyles: List<StyleOption>,
    val comfortStyles: List<StyleOption>,
)
