package io.dodn.springboot.core.domain.user

import io.dodn.springboot.core.domain.user.dto.UserRegisterRequest
import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.core.support.error.ErrorType
import io.dodn.springboot.storage.db.core.user.UserEntity
import io.dodn.springboot.storage.db.core.user.UserRepository
import io.dodn.springboot.storage.db.core.user.UserStatus
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) : UserDetailsService {
    override fun loadUserByUsername(email: String): UserDetails {
        val user = findByEmail(email)

        if (user.status != UserStatus.ACTIVE) {
            throw CoreException(ErrorType.USER_INACTIVE)
        }

        val authorities = listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))

        // We need to get the encoded password from the UserEntity
        val userEntity = userRepository.findByEmail(email)
            .orElseThrow { CoreException(ErrorType.USER_NOT_FOUND) }

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

    @Transactional(readOnly = true)
    fun findByEmail(email: String): UserInfo {
        return userRepository.findByEmail(email)
            .map { it.toUserInfo() }
            .orElseThrow { CoreException(ErrorType.USER_NOT_FOUND) }
    }

    @Transactional
    fun register(request: UserRegisterRequest): UserInfo {
        // Check if email already exists
        if (userRepository.existsByEmail(request.email)) {
            throw CoreException(ErrorType.EMAIL_ALREADY_EXISTS)
        }

        // Create new user
        val user = UserEntity(
            email = request.email,
            password = passwordEncoder.encode(request.password),
            name = request.name,
        )

        val savedUser = userRepository.save(user)
        return savedUser.toUserInfo()
    }

    @Transactional
    fun verifyCredentials(email: String, password: String): UserInfo {
        val user = userRepository.findByEmail(email)
            .orElseThrow { CoreException(ErrorType.INVALID_CREDENTIALS) }

        if (user.status != UserStatus.ACTIVE) {
            throw CoreException(ErrorType.USER_INACTIVE)
        }

        if (!passwordEncoder.matches(password, user.password)) {
            throw CoreException(ErrorType.INVALID_CREDENTIALS)
        }

        // Update last login time
        user.lastLoginAt = LocalDateTime.now()
        val updatedUser = userRepository.save(user)

        return updatedUser.toUserInfo()
    }

    // Extension function to convert UserEntity to UserInfo
    private fun UserEntity.toUserInfo(): UserInfo = UserInfo(
        id = this.id,
        email = this.email,
        name = this.name,
        status = this.status,
        role = this.role,
        lastLoginAt = this.lastLoginAt,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
    )
}
