package io.dodn.springboot.core.domain.user

import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.core.support.error.ErrorType
import io.dodn.springboot.storage.db.core.token.RefreshTokenRepository
import io.dodn.springboot.storage.db.core.user.UserEntity
import io.dodn.springboot.storage.db.core.user.UserRepository
import io.dodn.springboot.storage.db.core.user.UserStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@PreAuthorize("hasRole('ADMIN')")
class AdminUserService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    // 다른 연관 리포지토리들
) {
    @Transactional
    fun activateUser(userId: Long): Boolean {
        val user = userRepository.findById(userId)
            .orElseThrow { CoreException(ErrorType.USER_NOT_FOUND) }

        if (user.status == UserStatus.ACTIVE) {
            throw CoreException(ErrorType.USER_ALREADY_ACTIVE)
        }

        user.status = UserStatus.ACTIVE
        userRepository.save(user)

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
    fun hardDeleteUser(userId: Long): Boolean {
        val user = userRepository.findById(userId)
            .orElseThrow { CoreException(ErrorType.USER_NOT_FOUND) }

        // DELETED 상태의 계정만 하드 삭제 가능
        if (user.status != UserStatus.DELETED) {
            throw CoreException(ErrorType.USER_NOT_DELETED)
        }

        // 모든 관련 데이터 삭제
        refreshTokenRepository.deleteAllByUserId(userId)

        // 여기에 다른 연관 데이터 삭제 로직 추가
        // 예: passwordHistoryRepository.deleteAllByUserId(userId)
        //     emailVerificationTokenRepository.deleteAllByUserId(userId)
        //     등등

        // 최종적으로 사용자 삭제
        userRepository.delete(user)

        return true
    }

    @Transactional(readOnly = true)
    fun findDeletedUsers(page: Int, size: Int): Page<UserInfo> {
        val pageable = PageRequest.of(page, size)
        return userRepository.findByStatus(UserStatus.DELETED, pageable)
            .map { it.toUserInfo() }
    }

    // Extension function to convert UserEntity to UserInfo
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
