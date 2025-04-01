package io.dodn.springboot.core.domain.user

import io.dodn.springboot.core.domain.token.TokenManager
import io.dodn.springboot.core.domain.user.dto.UserDeletionRequestDto
import io.dodn.springboot.core.domain.user.dto.UserRegisterRequest
import io.dodn.springboot.storage.db.core.user.UserStatus
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userFinder: UserFinder,
    private val userCreator: UserCreator,
    private val userLocker: UserLocker,
    private val userActivator: UserActivator,
    private val userDeleter: UserDeleter,
    private val tokenManager: TokenManager,
    private val userPasswordManager: UserPasswordManager,
) {
    @Transactional(readOnly = true)
    fun findByEmail(email: String): UserInfo {
        return userFinder.findByEmail(email)
    }

    @Transactional
    fun verifyCredentials(email: String, password: String): UserInfo {
        val user = userFinder.findByEmailAndStatus(email, UserStatus.ACTIVE)
        userPasswordManager.verifyPassword(password, user.id)
        return userCreator.updateLastLogin(user.id)
    }

    // 계정 생성/수정
    @Transactional
    fun register(request: UserRegisterRequest): UserInfo {
        val encodedPassword = userPasswordManager.validateAndEncodeNewPassword(request.password)

        val user = userCreator.createUser(email = request.email, password = encodedPassword, name = request.name)

        userActivator.activate(user.id)
        return user
    }

    @Transactional
    fun changePassword(email: String, currentPassword: String, newPassword: String): UserInfo {
        val user = userFinder.findByEmail(email)
        val encodedPassword = userPasswordManager.validateAndEncodePasswordChange(
            userId = user.id,
            currentPassword = currentPassword,
            newPassword = newPassword,
        )
        return userCreator.changePassword(user.id, encodedPassword)
    }

    // 계정 상태 관리
    @Transactional
    fun activateUser(userId: Long): Boolean {
        return userActivator.activate(userId)
    }

    @Transactional
    fun inactivateUser(userId: Long): Boolean {
        return userActivator.inactivate(userId)
    }

    @Transactional
    fun lockUser(userId: Long): Boolean {
        return userLocker.lock(userId)
    }

    @Transactional
    fun unlockUser(userId: Long): Boolean {
        return userLocker.unlock(userId)
    }

    // 계정 삭제 관련
    @Transactional
    fun deleteAccount(userId: Long, request: UserDeletionRequestDto): Boolean {
        userPasswordManager.verifyPassword(request.password, userId)
        val result = userDeleter.deleteAccount(userId)
        tokenManager.invalidateAllTokens(userId)
        return result
    }

    @Transactional
    fun hardDeleteUser(userId: Long): Boolean {
        return userDeleter.hardDelete(userId)
    }

    @Transactional(readOnly = true)
    fun findDeletedUsers(page: Int, size: Int): Page<UserInfo> {
        return userFinder.findByStatus(UserStatus.DELETED, page, size)
    }
}
