package io.dodn.springboot.core.api.controller.v1

import io.dodn.springboot.core.domain.user.email.EmailVerificationService
import io.dodn.springboot.core.support.response.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth/email")
class EmailVerificationController(
    private val emailVerificationService: EmailVerificationService,
) {

    @PostMapping("/verify")
    fun verifyEmail(@RequestParam token: String): ResponseEntity<ApiResponse<Boolean>> {
        val result = emailVerificationService.verifyEmail(token)
        return ResponseEntity.ok(ApiResponse.success(result))
    }

    @PostMapping("/resend")
    fun resendVerificationEmail(@RequestBody email: String): ResponseEntity<ApiResponse<Boolean>> {
        val result = emailVerificationService.sendVerificationEmail(email)
        return ResponseEntity.ok(ApiResponse.success(result))
    }
}
