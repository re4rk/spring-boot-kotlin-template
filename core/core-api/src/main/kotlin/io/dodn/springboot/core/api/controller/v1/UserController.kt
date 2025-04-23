package io.dodn.springboot.core.api.controller.v1

import io.dodn.springboot.core.api.auth.AuthFacade
import io.dodn.springboot.core.api.controller.v1.request.UserDeletionRequest
import io.dodn.springboot.core.domain.user.UserInfo
import io.dodn.springboot.core.domain.user.UserService
import io.dodn.springboot.core.support.auth.GominUserDetails
import io.dodn.springboot.core.support.response.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val authFacade: AuthFacade,
    private val userService: UserService,
) {

    @GetMapping("/profile")
    fun getUserProfile(
        @AuthenticationPrincipal userDetails: GominUserDetails,
    ): ResponseEntity<ApiResponse<UserInfo>> {
        val currentUser = authFacade.getCurrentUser(userDetails)
        return ResponseEntity.ok(ApiResponse.success(currentUser))
    }

    @DeleteMapping("/me")
    fun deleteAccount(
        @RequestBody request: UserDeletionRequest,
        @AuthenticationPrincipal userDetails: GominUserDetails,
    ): ResponseEntity<ApiResponse<Boolean>> {
        val currentUser = authFacade.getCurrentUser(userDetails)

        val result =
            userService.deleteAccount(userId = currentUser.id, password = request.password, reason = request.reason)
        return ResponseEntity.ok(ApiResponse.success(result))
    }
}
