package io.dodn.springboot.core.domain.user

import io.dodn.springboot.core.domain.token.TokenManager
import io.dodn.springboot.core.domain.user.dto.UserDeletionRequestDto
import io.dodn.springboot.core.domain.user.password.PasswordManager
import io.dodn.springboot.storage.db.core.user.UserRepository
import org.springframework.stereotype.Component

@Component
class UserDeleter(
    private val userRepository: UserRepository,
    private val userFinder: UserFinder,
    private val passwordManager: PasswordManager,
    private val userCreator: UserCreator,
    private val tokenManager: TokenManager,
) {
    fun deleteAccount(email: String, request: UserDeletionRequestDto): Boolean {
        val user = userFinder.findByEmail(email)
        passwordManager.verifyPassword(request.password, user.id)
        userCreator.markAsDeleted(user.id)
        tokenManager.invalidateAllTokens(user.id)
        return true
    }

    fun hardDelete(userId: Long): Boolean {
        userRepository.deleteById(userId)
        return true
    }
}
