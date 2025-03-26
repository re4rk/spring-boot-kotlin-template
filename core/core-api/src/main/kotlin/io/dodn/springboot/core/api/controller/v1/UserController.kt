package io.dodn.springboot.core.api.controller.v1

import io.dodn.springboot.core.api.auth.AuthFacade
import io.dodn.springboot.core.domain.user.UserInfo
import io.dodn.springboot.core.support.response.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserController(private val authFacade: AuthFacade) {

    @GetMapping("/profile")
    fun getUserProfile(): ResponseEntity<ApiResponse<UserInfo>> {
        val currentUser = authFacade.getCurrentUser()
        return ResponseEntity.ok(ApiResponse.success(currentUser))
    }
}
