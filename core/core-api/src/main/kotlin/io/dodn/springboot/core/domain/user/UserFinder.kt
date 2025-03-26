package io.dodn.springboot.core.domain.user

import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.core.support.error.ErrorType
import io.dodn.springboot.storage.db.core.user.UserEntity
import io.dodn.springboot.storage.db.core.user.UserRepository
import io.dodn.springboot.storage.db.core.user.UserStatus
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component

@Component
class UserFinder(
    private val userRepository: UserRepository,
) {
    fun findByEmail(email: String): UserInfo {
        return userRepository.findByEmail(email)
            .map { it.toUserInfo() }
            .orElseThrow { CoreException(ErrorType.USER_NOT_FOUND) }
    }

    fun findByEmailAndStatus(email: String, status: UserStatus): UserInfo {
        val user = findByEmail(email)

        if (user.status != status) {
            throw CoreException(ErrorType.USER_INACTIVE)
        }

        return user
    }

    fun validateEmailNotExists(email: String) {
        if (userRepository.existsByEmail(email)) {
            throw CoreException(ErrorType.EMAIL_ALREADY_EXISTS)
        }
    }

    fun createUserDetails(email: String): UserDetails {
        val user = findByEmail(email)

        val userEntity = userRepository.findByEmail(email)
            .orElseThrow { CoreException(ErrorType.USER_NOT_FOUND) }

        val authorities = listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))

        return User.builder()
            .username(user.email)
            .password(userEntity.password)
            .authorities(authorities)
            .accountExpired(false)
            .accountLocked(user.status == UserStatus.LOCKED)
            .credentialsExpired(false)
            .disabled(false)
            .build()
    }

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
