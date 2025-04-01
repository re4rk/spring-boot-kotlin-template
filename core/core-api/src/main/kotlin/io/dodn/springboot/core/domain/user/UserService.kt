package io.dodn.springboot.core.domain.user

import io.dodn.springboot.core.domain.token.TokenManager
import io.dodn.springboot.core.domain.user.dto.UserDeletionRequestDto
import io.dodn.springboot.core.domain.user.dto.UserRegisterRequest
import io.dodn.springboot.core.domain.user.password.PasswordManager
import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.core.support.error.ErrorType
import io.dodn.springboot.storage.db.core.token.RefreshTokenRepository
import io.dodn.springboot.storage.db.core.user.UserEntity
import io.dodn.springboot.storage.db.core.user.UserRepository
import io.dodn.springboot.storage.db.core.user.UserStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
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
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
) : UserDetailsService {

    // 핵심 인증/계정 관련
    override fun loadUserByUsername(email: String): UserDetails {
        return userFinder.createUserDetails(email)
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
        userFinder.validateEmailNotExists(request.email)
        passwordManager.validateNewPassword(request.password)
        val user = userCreator.createUser(email = request.email, password = request.password, name = request.name)
        userCreator.markAsActive(user.id)
        return user
    }

    @Transactional
    fun changePassword(email: String, currentPassword: String, newPassword: String): Boolean {
        return passwordManager.changePassword(email, currentPassword, newPassword)
    }

    // 계정 상태 관리
    @Transactional
    fun activateUser(userId: Long): Boolean {
        val user = userRepository.findById(userId)
            .orElseThrow { CoreException(ErrorType.USER_NOT_FOUND) }

        if (user.status == UserStatus.ACTIVE) {
            throw CoreException(ErrorType.USER_ALREADY_ACTIVE)
        }
        return true
    }

    @Transactional
    fun inactivateUser(userId: Long): Boolean {
        val user = userRepository.findById(userId)
            .orElseThrow { CoreException(ErrorType.USER_NOT_FOUND) }

        if (user.status == UserStatus.INACTIVE) {
            throw CoreException(ErrorType.USER_ALREADY_INACTIVE)
        }

        user.status = UserStatus.INACTIVE
        userRepository.save(user)
        return true
    }

    @Transactional
    fun lockUser(userId: Long): Boolean {
        val user = userRepository.findById(userId)
            .orElseThrow { CoreException(ErrorType.USER_NOT_FOUND) }

        if (user.status == UserStatus.LOCKED) {
            throw CoreException(ErrorType.USER_ALREADY_LOCKED)
        }

        user.status = UserStatus.LOCKED
        userRepository.save(user)
        return true
    }

    @Transactional
    fun unlockUser(userId: Long): Boolean {
        val user = userRepository.findById(userId)
            .orElseThrow { CoreException(ErrorType.USER_NOT_FOUND) }

        if (user.status != UserStatus.LOCKED) {
            throw CoreException(ErrorType.USER_NOT_LOCKED)
        }

        user.status = UserStatus.ACTIVE
        userRepository.save(user)
        return true
    }

    // 계정 삭제 관련
    @Transactional
    fun deleteAccount(email: String, request: UserDeletionRequestDto): Boolean {
        val user = userFinder.findByEmail(email)
        passwordManager.verifyPassword(request.password, user.id)
        userCreator.markAsDeleted(user.id)
        tokenManager.invalidateAllTokens(user.id)
        return true
    }

    @Transactional
    fun hardDeleteUser(userId: Long): Boolean {
        val user = userRepository.findById(userId)
            .orElseThrow { CoreException(ErrorType.USER_NOT_FOUND) }

        if (user.status != UserStatus.DELETED) {
            throw CoreException(ErrorType.USER_NOT_DELETED)
        }

        refreshTokenRepository.deleteAllByUserId(userId)
        userRepository.delete(user)
        return true
    }

    @Transactional(readOnly = true)
    fun findDeletedUsers(page: Int, size: Int): Page<UserInfo> {
        val pageable = PageRequest.of(page, size)
        return userRepository.findByStatus(UserStatus.DELETED, pageable)
            .map { it.toUserInfo() }
    }

    // 유틸리티
    private fun UserEntity.toUserInfo(): UserInfo = UserInfo(
        id = this.id,
        email = this.email,
        name = this.name,
        status = this.status,
        role = this.role,
        lastLoginAt = this.lastLoginAt,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
    )
}
