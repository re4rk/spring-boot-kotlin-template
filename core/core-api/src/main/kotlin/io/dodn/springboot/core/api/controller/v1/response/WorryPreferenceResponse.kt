package io.dodn.springboot.core.api.controller.v1.response

import io.dodn.springboot.core.domain.worry.preference.ComfortStyle
import io.dodn.springboot.core.domain.worry.preference.ExpressionStyle
import io.dodn.springboot.core.domain.worry.preference.WorryPreference

/**
 * WorryPreference 응답 모델
 */
data class WorryPreferenceResponse(
    val id: Long,
    val userId: Long,
    val expressionStyle: ExpressionStyle,
    val comfortStyle: ComfortStyle,
) {
    companion object {
        fun from(preference: WorryPreference): WorryPreferenceResponse {
            return WorryPreferenceResponse(
                id = preference.id,
                userId = preference.userId,
                expressionStyle = preference.expressionStyle,
                comfortStyle = preference.comfortStyle,
            )
        }
    }
}
