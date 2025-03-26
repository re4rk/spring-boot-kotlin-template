package io.dodn.springboot.core.domain.user

import io.dodn.springboot.core.domain.user.dto.UserRegisterRequest
import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.core.support.error.ErrorType
import io.dodn.springboot.storage.db.core.user.UserEntity
import io.dodn.springboot.storage.db.core.user.UserRepository
import io.dodn.springboot.storage.db.core.user.UserStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class UserService(
    private val userRepository: UserRepository,
) {

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
            password = request.password,
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

        if (password != user.password) {
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
