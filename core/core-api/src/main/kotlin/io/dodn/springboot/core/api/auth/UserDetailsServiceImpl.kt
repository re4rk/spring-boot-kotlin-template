package io.dodn.springboot.core.api.auth

import io.dodn.springboot.core.domain.user.UserService
import io.dodn.springboot.core.support.auth.GominUserDetails
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserDetailsServiceImpl(
    private val userService: UserService,
) : UserDetailsService {
    @Transactional(readOnly = true)
    override fun loadUserByUsername(email: String): UserDetails {
        val userInfo = userService.findByEmail(email)

        return GominUserDetails(
            id = userInfo.id,
            email = userInfo.email,
            encodedPassword = userInfo.password,
            name = userInfo.name,
            status = userInfo.status,
            role = userInfo.role,
            lastLoginAt = userInfo.lastLoginAt,
            createdAt = userInfo.createdAt,
            updatedAt = userInfo.updatedAt,
        )
    }
}
