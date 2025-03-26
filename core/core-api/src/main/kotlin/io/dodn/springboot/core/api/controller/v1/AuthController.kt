package io.dodn.springboot.core.api.controller.v1

import io.dodn.springboot.core.api.auth.AuthService
import io.dodn.springboot.core.domain.user.dto.AuthResponse
import io.dodn.springboot.core.domain.user.dto.RefreshTokenRequest
import io.dodn.springboot.core.domain.user.UserInfo
import io.dodn.springboot.core.domain.user.dto.UserLoginRequest
import io.dodn.springboot.core.domain.user.dto.UserRegisterRequest
import io.dodn.springboot.core.support.response.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/register")
    fun register(@RequestBody request: UserRegisterRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        val authResponse = authService.register(request)
        return ResponseEntity.ok(ApiResponse.success(authResponse))
    }

    @PostMapping("/login")
    fun login(@RequestBody request: UserLoginRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        val authResponse = authService.login(request)
        return ResponseEntity.ok(ApiResponse.success(authResponse))
    }

    @PostMapping("/refresh")
    fun refreshToken(@RequestBody request: RefreshTokenRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        val authResponse = authService.refreshToken(request)
        return ResponseEntity.ok(ApiResponse.success(authResponse))
    }

    @PostMapping("/logout")
    fun logout(): ResponseEntity<ApiResponse<Any>> {
        authService.logout()
        return ResponseEntity.ok(ApiResponse.success())
    }

    @GetMapping("/me")
    fun getCurrentUser(): ResponseEntity<ApiResponse<UserInfo>> {
        val userInfo = authService.getCurrentUser()
        return ResponseEntity.ok(ApiResponse.success(userInfo))
    }
}
