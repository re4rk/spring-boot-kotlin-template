package io.dodn.springboot.core.domain.user

import io.dodn.springboot.core.domain.token.TokenManager
import io.dodn.springboot.core.domain.user.dto.UserDeletionRequestDto
import io.dodn.springboot.core.domain.user.dto.UserRegisterRequest
import io.dodn.springboot.core.domain.user.password.PasswordManager
import io.dodn.springboot.core.domain.user.password.PasswordPolicy
import io.dodn.springboot.storage.db.core.user.UserStatus
import org.springframework.data.domain.Page
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userFinder: UserFinder,
    private val userCreator: UserCreator,
    private val userLocker: UserLocker,
    private val userActivator: UserActivator,
    private val userDeleter: UserDeleter,
    private val passwordManager: PasswordManager,
    private val tokenManager: TokenManager,
    private val passwordEncoder: PasswordEncoder,
    private val passwordPolicy: PasswordPolicy,
) : UserDetailsService {

    // 핵심 인증/계정 관련
    @Transactional(readOnly = true)
    override fun loadUserByUsername(email: String): UserDetails {
        val user = findByEmail(email)

        return User.builder()
            .username(user.email)
            .password(user.password)
            .authorities(listOf(SimpleGrantedAuthority("ROLE_${user.role.name}")))
            .accountExpired(false)
            .accountLocked(user.status == UserStatus.LOCKED)
            .credentialsExpired(false)
            .disabled(false)
            .build()
    }

    @Transactional(readOnly = true)
    fun findByEmail(email: String): UserInfo {
        return userFinder.findByEmail(email)
    }

    @Transactional
    fun verifyCredentials(email: String, password: String): UserInfo {
        val user = userFinder.findByEmailAndStatus(email, UserStatus.ACTIVE)
        passwordManager.verifyPassword(password, user.id)
        return userCreator.updateLastLogin(user.id)
    }

    // 계정 생성/수정
    @Transactional
    fun register(request: UserRegisterRequest): UserInfo {
        passwordManager.validateNewPassword(request.password)

        val encodedPassword = passwordEncoder.encode(request.password)

        val user = userCreator.createUser(email = request.email, password = encodedPassword, name = request.name)

        passwordPolicy.addPasswordToHistory(user.id, encodedPassword)

        userActivator.activate(user.id)
        return user
    }

    @Transactional
    fun changePassword(email: String, currentPassword: String, newPassword: String): Boolean {
        return passwordManager.changePassword(email, currentPassword, newPassword)
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
        val result = userDeleter.deleteAccount(userId, request)

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
