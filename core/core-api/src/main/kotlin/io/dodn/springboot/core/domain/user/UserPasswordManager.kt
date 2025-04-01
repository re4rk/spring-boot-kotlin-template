package io.dodn.springboot.core.domain.user

import io.dodn.springboot.core.domain.user.password.PasswordPolicy
import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.core.support.error.ErrorType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class UserPasswordManager(
    private val userFinder: UserFinder,
    private val passwordEncoder: PasswordEncoder,
    private val passwordPolicy: PasswordPolicy,
) {
    fun verifyPassword(password: String, userId: Long) {
        val user = userFinder.findById(userId)
        if (!passwordEncoder.matches(password, user.password)) {
            throw CoreException(ErrorType.INVALID_CREDENTIALS)
        }
    }

    fun validateAndEncodeNewPassword(password: String): String {
        if (!passwordPolicy.isValidPassword(password)) {
            throw CoreException(ErrorType.WEAK_PASSWORD)
        }
        return passwordEncoder.encode(password)
    }

    fun validateAndEncodePasswordChange(userId: Long, currentPassword: String, newPassword: String): String {
        // 현재 비밀번호 확인
        verifyPassword(currentPassword, userId)

        // 새 비밀번호 검증 및 인코딩
        val encodedPassword = passwordPolicy.validateAndEncodePassword(userId, newPassword)

        // 비밀번호 이력 추가
        passwordPolicy.addPasswordToHistory(userId, encodedPassword)

        return encodedPassword
    }
}
