package io.dodn.springboot.core.api.controller.v1

import io.dodn.springboot.core.api.controller.v1.request.WorryPreferenceRequest
import io.dodn.springboot.core.api.controller.v1.response.StyleOption
import io.dodn.springboot.core.api.controller.v1.response.WorryPreferenceOptionsResponse
import io.dodn.springboot.core.api.controller.v1.response.WorryPreferenceResponse
import io.dodn.springboot.core.domain.worry.preference.ComfortStyle
import io.dodn.springboot.core.domain.worry.preference.ExpressionStyle
import io.dodn.springboot.core.domain.worry.preference.WorryPreferenceParams
import io.dodn.springboot.core.domain.worry.preference.WorryPreferenceService
import io.dodn.springboot.core.support.auth.GominUserDetails
import io.dodn.springboot.core.support.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/worry-preferences")
class WorryPreferenceController(
    private val worryPreferenceService: WorryPreferenceService,
) {

    @PostMapping
    fun createWorryPreference(
        @RequestBody @Valid request: WorryPreferenceRequest,
        @AuthenticationPrincipal userDetails: GominUserDetails,
    ): ResponseEntity<ApiResponse<WorryPreferenceResponse>> {
        val params = WorryPreferenceParams(
            expressionStyle = request.expressionStyle,
            comfortStyle = request.comfortStyle,
        )

        val preference = worryPreferenceService.createWorryPreference(userDetails.id, params)

        return ResponseEntity.ok(ApiResponse.success(WorryPreferenceResponse.from(preference)))
    }

    @GetMapping("/me")
    fun getMyWorryPreference(
        @AuthenticationPrincipal userDetails: GominUserDetails,
    ): ResponseEntity<ApiResponse<WorryPreferenceResponse>> {
        val preference = worryPreferenceService.getWorryPreferenceByUserId(userDetails.id)

        return ResponseEntity.ok(ApiResponse.success(WorryPreferenceResponse.from(preference)))
    }

    @PutMapping("/me")
    fun updateMyWorryPreference(
        @RequestBody @Valid request: WorryPreferenceRequest,
        @AuthenticationPrincipal userDetails: GominUserDetails,
    ): ResponseEntity<ApiResponse<WorryPreferenceResponse>> {
        val params = WorryPreferenceParams(
            expressionStyle = request.expressionStyle,
            comfortStyle = request.comfortStyle,
        )

        val preference = worryPreferenceService.updateWorryPreference(userDetails.id, params)

        return ResponseEntity.ok(ApiResponse.success(WorryPreferenceResponse.from(preference)))
    }

    @GetMapping("/options")
    fun getWorryPreferenceOptions(): ResponseEntity<ApiResponse<WorryPreferenceOptionsResponse>> {
        val expressionStyles = ExpressionStyle.entries.map {
            StyleOption(it.name, getExpressionStyleDescription(it))
        }

        val comfortStyles = ComfortStyle.entries.map {
            StyleOption(it.name, getComfortStyleDescription(it))
        }

        val response = WorryPreferenceOptionsResponse(
            expressionStyles = expressionStyles,
            comfortStyles = comfortStyles,
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    private fun getExpressionStyleDescription(style: ExpressionStyle): String {
        return when (style) {
            ExpressionStyle.DIRECT -> "감정을 직접적으로 표현하는 스타일"
            ExpressionStyle.INDIRECT -> "감정을 우회적으로 표현하는 스타일"
        }
    }

    private fun getComfortStyleDescription(style: ComfortStyle): String {
        return when (style) {
            ComfortStyle.PRACTICAL -> "실용적인 해결책을 제시하는 위로 스타일"
            ComfortStyle.EMOTIONAL -> "감정에 공감하는 위로 스타일"
            ComfortStyle.SUPPORTIVE -> "지지와 격려를 중심으로 하는 위로 스타일"
        }
    }
}

