package io.dodn.springboot.core.domain.user

import io.dodn.springboot.core.domain.user.password.PasswordPolicy
import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.core.support.error.ErrorType
import io.dodn.springboot.storage.db.core.user.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class UserPasswordManager(
    private val userFinder: UserFinder,
    private val passwordEncoder: PasswordEncoder,
    private val passwordPolicy: PasswordPolicy,
    private val userRepository: UserRepository,
) {
    fun verifyPassword(password: String, userId: Long) {
        val user = userFinder.findById(userId)
        if (!passwordEncoder.matches(password, user.password)) {
            throw CoreException(ErrorType.INVALID_CREDENTIALS)
        }
    }

    @Transactional
    fun changePassword(userId: Long, newPassword: String): UserInfo {
        val user = userRepository.findById(userId)
            .orElseThrow { CoreException(ErrorType.USER_NOT_FOUND) }

        // 새 비밀번호 검증 및 인코딩
        user.password = passwordPolicy.validateAndEncodePassword(userId, newPassword)
        val updatedUser = userRepository.save(user)

        // 비밀번호 이력 추가
        passwordPolicy.addPasswordToHistory(userId, user.password)

        return updatedUser.toUserInfo()
    }
}
