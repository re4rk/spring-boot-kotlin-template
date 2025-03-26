package io.dodn.springboot.core.api.service

import io.dodn.springboot.core.domain.user.UserService
import io.dodn.springboot.storage.db.core.user.UserRepository
import io.dodn.springboot.storage.db.core.user.UserStatus
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl(
    private val userService: UserService,
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(email: String): UserDetails {
        val user = userService.findByEmail(email)
            ?: throw UsernameNotFoundException("User not found with email: $email")

        if (user.status != UserStatus.ACTIVE) {
            throw UsernameNotFoundException("User is not active: $email")
        }

        val authorities = listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))

        // We need to get the encoded password from the UserEntity
        val userEntity = userRepository.findByEmail(email)
            .orElseThrow { UsernameNotFoundException("User not found with email: $email") }

        return User.builder()
            .username(user.email)
            .password(userEntity.password) // Use the encoded password from the entity
            .authorities(authorities)
            .accountExpired(false)
            .accountLocked(user.status == UserStatus.LOCKED)
            .credentialsExpired(false)
            .disabled(user.status != UserStatus.ACTIVE)
            .build()
    }
}