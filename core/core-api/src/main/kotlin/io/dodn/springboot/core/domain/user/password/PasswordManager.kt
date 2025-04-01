package io.dodn.springboot.core.domain.user.password

import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.core.support.error.ErrorType
import io.dodn.springboot.storage.db.core.user.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class PasswordManager(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val passwordPolicy: PasswordPolicy,
) {
    fun validateNewPassword(password: String) {
        if (!passwordPolicy.isValidPassword(password)) {
            throw CoreException(ErrorType.WEAK_PASSWORD)
        }
    }

    fun verifyPassword(password: String, userId: Long) {
        val user = userRepository.findById(userId)
            .orElseThrow { CoreException(ErrorType.USER_NOT_FOUND) }

        if (!passwordEncoder.matches(password, user.password)) {
            throw CoreException(ErrorType.INVALID_CREDENTIALS)
        }
    }

    fun changePassword(email: String, currentPassword: String, newPassword: String): Boolean {
        val user = userRepository.findByEmail(email)
            .orElseThrow { CoreException(ErrorType.USER_NOT_FOUND) }

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, user.password)) {
            throw CoreException(ErrorType.INVALID_CREDENTIALS)
        }

        // 비밀번호 정책 검증 및 인코딩 (이력 체크 포함)
        val encodedPassword = passwordPolicy.validateAndEncodePassword(
            userId = user.id,
            password = newPassword,
        )

        // 비밀번호 업데이트
        user.password = encodedPassword
        userRepository.save(user)

        return true
    }
}
