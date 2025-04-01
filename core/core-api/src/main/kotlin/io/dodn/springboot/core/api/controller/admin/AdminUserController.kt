package io.dodn.springboot.core.api.controller.admin

import io.dodn.springboot.core.domain.user.UserInfo
import io.dodn.springboot.core.domain.user.UserService
import io.dodn.springboot.core.support.response.ApiResponse
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
class AdminUserController(
    private val userService: UserService,
) {
    @PostMapping("/{userId}/activate")
    fun activateUser(@PathVariable userId: Long): ResponseEntity<ApiResponse<Boolean>> {
        val result = userService.activateUser(userId)
        return ResponseEntity.ok(ApiResponse.success(result))
    }

    @PostMapping("/{userId}/inactivate")
    fun inactivateUser(@PathVariable userId: Long): ResponseEntity<ApiResponse<Boolean>> {
        val result = userService.inactivateUser(userId)
        return ResponseEntity.ok(ApiResponse.success(result))
    }

    @PostMapping("/{userId}/lock")
    fun lockUser(@PathVariable userId: Long): ResponseEntity<ApiResponse<Boolean>> {
        val result = userService.lockUser(userId)
        return ResponseEntity.ok(ApiResponse.success(result))
    }

    @PostMapping("/{userId}/unlock")
    fun unlockUser(@PathVariable userId: Long): ResponseEntity<ApiResponse<Boolean>> {
        val result = userService.unlockUser(userId)
        return ResponseEntity.ok(ApiResponse.success(result))
    }

    @DeleteMapping("/{userId}")
    fun hardDeleteUser(@PathVariable userId: Long): ResponseEntity<ApiResponse<Boolean>> {
        val result = userService.hardDeleteUser(userId)
        return ResponseEntity.ok(ApiResponse.success(result))
    }

    @GetMapping("/deleted")
    fun getDeletedUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<ApiResponse<Page<UserInfo>>> {
        val users = userService.findDeletedUsers(page, size)
        return ResponseEntity.ok(ApiResponse.success(users))
    }
}
