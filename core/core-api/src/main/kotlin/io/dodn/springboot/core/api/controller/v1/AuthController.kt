package io.dodn.springboot.core.api.controller.v1

import io.dodn.springboot.core.api.auth.AuthFacade
import io.dodn.springboot.core.api.auth.AuthResponse
import io.dodn.springboot.core.api.auth.RefreshTokenRequest
import io.dodn.springboot.core.api.auth.RegisterResponse
import io.dodn.springboot.core.domain.user.UserInfo
import io.dodn.springboot.core.api.controller.v1.request.UserChangePasswordRequestDto
import io.dodn.springboot.core.api.controller.v1.request.UserLoginRequestDto
import io.dodn.springboot.core.domain.user.UserRegisterRequest
import io.dodn.springboot.core.support.response.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(private val authFacade: AuthFacade) {

    @PostMapping("/register")
    fun register(@RequestBody request: UserRegisterRequest): ResponseEntity<ApiResponse<RegisterResponse>> {
        val authResponse = authFacade.register(request)
        return ResponseEntity.ok(ApiResponse.success(authResponse))
    }

    @PostMapping("/login")
    fun login(@RequestBody request: UserLoginRequestDto): ResponseEntity<ApiResponse<AuthResponse>> {
        val authResponse = authFacade.login(request)
        return ResponseEntity.ok(ApiResponse.success(authResponse))
    }

    @PostMapping("/refresh")
    fun refreshToken(@RequestBody request: RefreshTokenRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        val authResponse = authFacade.refreshToken(request)
        return ResponseEntity.ok(ApiResponse.success(authResponse))
    }

    @PostMapping("/logout")
    fun logout(): ResponseEntity<ApiResponse<Any>> {
        authFacade.logout()
        return ResponseEntity.ok(ApiResponse.success())
    }

    @PostMapping("/change-password")
    fun changePassword(@RequestBody request: UserChangePasswordRequestDto): ResponseEntity<ApiResponse<Boolean>> {
        val result = authFacade.changePassword(request)
        return ResponseEntity.ok(ApiResponse.success(result))
    }

    @GetMapping("/me")
    fun getCurrentUser(): ResponseEntity<ApiResponse<UserInfo>> {
        val userInfo = authFacade.getCurrentUser()
        return ResponseEntity.ok(ApiResponse.success(userInfo))
    }
}
