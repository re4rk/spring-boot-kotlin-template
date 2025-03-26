package io.dodn.springboot.core.domain.user

import io.dodn.springboot.core.domain.user.password.PasswordPolicyService
import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.core.support.error.ErrorType
import io.dodn.springboot.storage.db.core.user.UserEntity
import io.dodn.springboot.storage.db.core.user.UserRepository
import io.dodn.springboot.storage.db.core.user.UserStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class UserCreator(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val passwordPolicyService: PasswordPolicyService,
) {
    fun createUser(email: String, password: String, name: String?): UserInfo {
        // 비밀번호 인코딩
        val encodedPassword = passwordEncoder.encode(password)

        // 사용자 생성
        val user = UserEntity(
            email = email,
            password = encodedPassword,
            name = name,
            status = UserStatus.PENDING_VERIFICATION,
        )

        val savedUser = userRepository.save(user)

        // 비밀번호 이력에 추가
        passwordPolicyService.addPasswordToHistory(savedUser.id, encodedPassword)

        return savedUser.toUserInfo()
    }

    fun updateLastLogin(userId: Long): UserInfo {
        val user = userRepository.findById(userId)
            .orElseThrow { CoreException(ErrorType.USER_NOT_FOUND) }

        user.lastLoginAt = LocalDateTime.now()
        val updatedUser = userRepository.save(user)

        return updatedUser.toUserInfo()
    }

    fun markAsDeleted(userId: Long): UserInfo {
        val user = userRepository.findById(userId)
            .orElseThrow { CoreException(ErrorType.USER_NOT_FOUND) }

        user.status = UserStatus.DELETED
        user.lastLoginAt = LocalDateTime.now() // 삭제 시간으로 사용

        val updatedUser = userRepository.save(user)

        return updatedUser.toUserInfo()
    }

    fun markAsActive(userId: Long): UserInfo {
        val user = userRepository.findById(userId)
            .orElseThrow { CoreException(ErrorType.USER_NOT_FOUND) }

        user.status = UserStatus.ACTIVE
        val updatedUser = userRepository.save(user)

        return updatedUser.toUserInfo()
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
