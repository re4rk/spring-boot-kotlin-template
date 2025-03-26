package io.dodn.springboot.core.domain.user

import io.dodn.springboot.core.domain.token.TokenManager
import io.dodn.springboot.core.domain.user.dto.UserDeletionRequestDto
import io.dodn.springboot.core.domain.user.dto.UserRegisterRequest
import io.dodn.springboot.core.domain.user.password.PasswordManager
import io.dodn.springboot.storage.db.core.user.UserStatus
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userFinder: UserFinder,
    private val userCreator: UserCreator,
    private val passwordManager: PasswordManager,
    private val tokenManager: TokenManager,
) : UserDetailsService {
    override fun loadUserByUsername(email: String): UserDetails {
        return userFinder.createUserDetails(email)
    }

    @Transactional(readOnly = true)
    fun findByEmail(email: String): UserInfo {
        return userFinder.findByEmail(email)
    }

    @Transactional
    fun register(request: UserRegisterRequest): UserInfo {
        userFinder.validateEmailNotExists(request.email)

        passwordManager.validateNewPassword(request.password)

        val user = userCreator.createUser(email = request.email, password = request.password, name = request.name)

        // 임시 : 사용자 활성화 처리
        userCreator.markAsActive(user.id)

        return user
    }

    @Transactional
    fun changePassword(email: String, currentPassword: String, newPassword: String): Boolean {
        return passwordManager.changePassword(email, currentPassword, newPassword)
    }

    @Transactional
    fun verifyCredentials(email: String, password: String): UserInfo {
        val user = userFinder.findByEmailAndStatus(email, UserStatus.ACTIVE)

        passwordManager.verifyPassword(password, user.id)

        return userCreator.updateLastLogin(user.id)
    }

    @Transactional
    fun deleteAccount(email: String, request: UserDeletionRequestDto): Boolean {
        val user = userFinder.findByEmail(email)

        passwordManager.verifyPassword(request.password, user.id)

        userCreator.markAsDeleted(user.id)

        tokenManager.invalidateAllTokens(user.id)

        return true
    }
}
