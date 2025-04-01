package io.dodn.springboot.core.api.auth

import io.dodn.springboot.core.domain.user.UserService
import io.dodn.springboot.storage.db.core.user.UserStatus
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
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
        val user = userService.findByEmail(email)

        return User.builder()
            .username(user.email)
            .password(user.password)
            .authorities(listOf(SimpleGrantedAuthority("ROLE_${user.role.name}")))
            .accountExpired(false)
            .accountLocked(user.status == UserStatus.LOCKED)
            .credentialsExpired(false)
            .disabled(false)
            .build()
    }
}
