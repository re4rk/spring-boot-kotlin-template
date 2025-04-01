package io.dodn.springboot.core.api.controller.v1

import io.dodn.springboot.core.domain.user.password.PasswordPolicy
import io.dodn.springboot.core.domain.user.password.PasswordResetRequestDto
import io.dodn.springboot.core.domain.user.password.PasswordResetService
import io.dodn.springboot.core.domain.user.password.PasswordResetVerifyDto
import io.dodn.springboot.core.support.response.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth/password")
class PasswordResetController(
    private val passwordResetService: PasswordResetService,
    private val passwordPolicy: PasswordPolicy,
) {

    @PostMapping("/reset-request")
    fun requestPasswordReset(@RequestBody request: PasswordResetRequestDto): ResponseEntity<ApiResponse<Boolean>> {
        val result = passwordResetService.requestPasswordReset(request)
        return ResponseEntity.ok(ApiResponse.success(result))
    }

    @PostMapping("/reset")
    fun resetPassword(@RequestBody request: PasswordResetVerifyDto): ResponseEntity<ApiResponse<Boolean>> {
        val result = passwordResetService.resetPassword(request)
        return ResponseEntity.ok(ApiResponse.success(result))
    }

    @GetMapping("/policy")
    fun getPasswordPolicy(): ResponseEntity<ApiResponse<Map<String, Any>>> {
        val policy = mapOf(
            "minLength" to 8,
            "requireDigit" to true,
            "requireLowercase" to true,
            "requireUppercase" to true,
            "requireSpecial" to true,
            "historyCount" to 5,
        )
        return ResponseEntity.ok(ApiResponse.success(policy))
    }

    @PostMapping("/validate")
    fun validatePassword(@RequestBody password: String): ResponseEntity<ApiResponse<Map<String, Boolean>>> {
        val validations = passwordPolicy.validatePasswordStrength(password)
        return ResponseEntity.ok(ApiResponse.success(validations))
    }
}
