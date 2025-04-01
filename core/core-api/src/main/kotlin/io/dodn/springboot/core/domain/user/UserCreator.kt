package io.dodn.springboot.core.domain.user

import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.core.support.error.ErrorType
import io.dodn.springboot.storage.db.core.user.UserEntity
import io.dodn.springboot.storage.db.core.user.UserRepository
import io.dodn.springboot.storage.db.core.user.UserStatus
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class UserCreator(
    private val userRepository: UserRepository,
) {
    @Transactional
    fun createUser(email: String, password: String, name: String?): UserInfo {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(email)) {
            throw CoreException(ErrorType.EMAIL_ALREADY_EXISTS)
        }

        // 사용자 생성
        val user = UserEntity(
            email = email,
            password = password,
            name = name,
            status = UserStatus.PENDING_VERIFICATION,
        )

        val savedUser = userRepository.save(user)

        return savedUser.toUserInfo()
    }

    @Transactional
    fun changePassword(userId: Long, password: String): UserInfo {
        val user = userRepository.findById(userId)
            .orElseThrow { CoreException(ErrorType.USER_NOT_FOUND) }

        user.password = password
        val updatedUser = userRepository.save(user)

        return updatedUser.toUserInfo()
    }

    @Transactional
    fun updateLastLogin(userId: Long): UserInfo {
        val user = userRepository.findById(userId)
            .orElseThrow { CoreException(ErrorType.USER_NOT_FOUND) }

        user.lastLoginAt = LocalDateTime.now()
        val updatedUser = userRepository.save(user)

        return updatedUser.toUserInfo()
    }
}
