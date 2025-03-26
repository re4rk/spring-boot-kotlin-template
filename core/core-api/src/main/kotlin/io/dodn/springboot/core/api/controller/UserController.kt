package io.dodn.springboot.core.api.controller

import io.dodn.springboot.core.api.auth.AuthService
import io.dodn.springboot.core.domain.user.UserInfo
import io.dodn.springboot.core.support.response.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController(private val authService: AuthService) {

    @GetMapping("/profile")
    fun getUserProfile(): ResponseEntity<ApiResponse<UserInfo>> {
        val currentUser = authService.getCurrentUser()
        return ResponseEntity.ok(ApiResponse.success(currentUser))
    }
}
