
package io.dodn.springboot.core.api.controller

import io.dodn.springboot.core.api.auth.AuthService
import io.dodn.springboot.core.support.response.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin")
class AdminController(private val authService: AuthService) {

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    fun getAllUsers(): ResponseEntity<ApiResponse<String>> {
        // This is just a placeholder - in a real app, you would get all users
        return ResponseEntity.ok(ApiResponse.success("Admin access - List of all users would be returned here"))
    }
}
