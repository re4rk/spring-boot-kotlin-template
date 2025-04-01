package io.dodn.springboot.core.domain.user

import io.dodn.springboot.core.domain.token.TokenManager
import io.dodn.springboot.core.domain.user.dto.UserDeletionRequestDto
import io.dodn.springboot.core.domain.user.dto.UserRegisterRequest
import io.dodn.springboot.core.domain.user.password.PasswordResetManager
import io.dodn.springboot.storage.db.core.user.UserStatus
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userFinder: UserFinder,
    private val userPasswordManager: UserPasswordManager,
    private val userStateProcessor: UserStateProcessor,
    private val tokenManager: TokenManager,
    private val passwordResetManager: PasswordResetManager,
) {
    @Transactional(readOnly = true)
    fun findByEmail(email: String): UserInfo {
        return userFinder.findByEmail(email)
    }

    @Transactional
    fun verifyCredentials(email: String, password: String): UserInfo {
        val user = userFinder.findByEmailAndStatus(email, UserStatus.ACTIVE)

        userPasswordManager.verifyPassword(password, user.id)

        return userStateProcessor.updateLastLogin(user.id)
    }

    // 계정 생성/수정
    @Transactional
    fun register(request: UserRegisterRequest): UserInfo {
        val user = userStateProcessor.createUser(email = request.email, password = "", name = request.name)

        userPasswordManager.changePassword(user.id, request.password)

        userStateProcessor.activate(user.id)

        return user
    }

    @Transactional
    fun changePassword(email: String, currentPassword: String, newPassword: String): UserInfo {
        val user = userFinder.findByEmail(email)

        userPasswordManager.verifyPassword(currentPassword, user.id)

        return userPasswordManager.changePassword(user.id, newPassword)
    }

    @Transactional
    fun requestPasswordReset(email: String): Boolean {
        val user = userFinder.findByEmail(email)

        return passwordResetManager.requestPasswordReset(user.id, user.email)
    }

    @Transactional
    fun resetPassword(token: String, newPassword: String): Boolean {
        val validResult = passwordResetManager.verifyPasswordResetToken(token)

        val user = userFinder.findById(validResult.userId)

        userPasswordManager.changePassword(user.id, newPassword)

        return true
    }

    // 계정 상태 관리
    @Transactional
    fun activateUser(userId: Long): Boolean {
        return userStateProcessor.activate(userId)
    }

    @Transactional
    fun inactivateUser(userId: Long): Boolean {
        return userStateProcessor.inactivate(userId)
    }

    @Transactional
    fun lockUser(userId: Long): Boolean {
        return userStateProcessor.lock(userId)
    }

    @Transactional
    fun unlockUser(userId: Long): Boolean {
        return userStateProcessor.unlock(userId)
    }

    // 계정 삭제 관련
    @Transactional
    fun deleteAccount(userId: Long, request: UserDeletionRequestDto): Boolean {
        userPasswordManager.verifyPassword(request.password, userId)

        val result = userStateProcessor.deleteAccount(userId)

        tokenManager.invalidateAllTokens(userId)

        return result
    }

    @Transactional
    fun hardDeleteUser(userId: Long): Boolean {
        return userStateProcessor.hardDelete(userId)
    }

    @Transactional(readOnly = true)
    fun findDeletedUsers(page: Int, size: Int): Page<UserInfo> {
        return userFinder.findByStatus(UserStatus.DELETED, page, size)
    }
}
