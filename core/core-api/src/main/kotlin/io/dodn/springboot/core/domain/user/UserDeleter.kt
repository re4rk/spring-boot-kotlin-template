package io.dodn.springboot.core.domain.user

import io.dodn.springboot.core.domain.token.TokenManager
import io.dodn.springboot.core.domain.user.dto.UserDeletionRequestDto
import io.dodn.springboot.core.domain.user.password.PasswordManager
import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.core.support.error.ErrorType
import io.dodn.springboot.storage.db.core.user.UserRepository
import io.dodn.springboot.storage.db.core.user.UserStatus
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class UserDeleter(
    private val userRepository: UserRepository,
    private val passwordManager: PasswordManager,
    private val tokenManager: TokenManager,
) {

    fun deleteAccount(email: String, request: UserDeletionRequestDto): Boolean {
        val user = userRepository.findByEmail(email)
            .orElseThrow { CoreException(ErrorType.USER_NOT_FOUND) }

        passwordManager.verifyPassword(request.password, user.id)

        user.status = UserStatus.DELETED
        user.lastLoginAt = LocalDateTime.now()

        userRepository.save(user)

        tokenManager.invalidateAllTokens(user.id)
        return true
    }

    fun hardDelete(userId: Long): Boolean {
        userRepository.deleteById(userId)
        return true
    }
}
