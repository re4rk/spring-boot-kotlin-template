package io.dodn.springboot.core.domain.user

import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.core.support.error.ErrorType
import io.dodn.springboot.storage.db.core.user.UserEntity
import io.dodn.springboot.storage.db.core.user.UserRepository
import io.dodn.springboot.storage.db.core.user.UserStatus
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class UserStateProcessor(
    private val userRepository: UserRepository,
) {
    @Transactional
    fun createUser(email: String, password: String, name: String?): UserInfo {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(email)) {
            throw CoreException(ErrorType.EMAIL_ALREADY_EXISTS)
        }

        // 사용자 생성
        val user = UserEntity(
            email = email,
            password = password,
            name = name,
            status = UserStatus.PENDING_VERIFICATION,
        )

        val savedUser = userRepository.save(user)

        return savedUser.toUserInfo()
    }

    @Transactional
    fun activate(userId: Long): Boolean {
        val user = userRepository.findByIdWithOptimisticLock(userId)
            .orElseThrow { CoreException(ErrorType.USER_NOT_FOUND) }

        if (user.status == UserStatus.ACTIVE) {
            throw CoreException(ErrorType.USER_ALREADY_ACTIVE)
        }

        user.status = UserStatus.ACTIVE
        userRepository.save(user)
        return true
    }

    @Transactional
    fun inactivate(userId: Long): Boolean {
        val user = userRepository.findByIdWithOptimisticLock(userId)
            .orElseThrow { CoreException(ErrorType.USER_NOT_FOUND) }

        if (user.status == UserStatus.INACTIVE) {
            throw CoreException(ErrorType.USER_ALREADY_INACTIVE)
        }

        user.status = UserStatus.INACTIVE
        userRepository.save(user)
        return true
    }

    @Transactional
    fun lock(userId: Long): Boolean {
        try {
            val user = userRepository.findByIdWithOptimisticLock(userId)
                .orElseThrow { CoreException(ErrorType.USER_NOT_FOUND) }

            if (user.status == UserStatus.LOCKED) {
                throw CoreException(ErrorType.USER_ALREADY_LOCKED)
            }

            user.status = UserStatus.LOCKED
            userRepository.save(user)
            return true
        } catch (e: ObjectOptimisticLockingFailureException) {
            throw CoreException(ErrorType.CONCURRENT_MODIFICATION)
        }
    }

    @Transactional
    fun unlock(userId: Long): Boolean {
        try {
            val user = userRepository.findByIdWithOptimisticLock(userId)
                .orElseThrow { CoreException(ErrorType.USER_NOT_FOUND) }

            if (user.status != UserStatus.LOCKED) {
                throw CoreException(ErrorType.USER_NOT_LOCKED)
            }

            user.status = UserStatus.ACTIVE
            userRepository.save(user)
            return true
        } catch (e: ObjectOptimisticLockingFailureException) {
            throw CoreException(ErrorType.CONCURRENT_MODIFICATION)
        }
    }

    @Transactional
    fun deleteAccount(userId: Long): Boolean {
        val user = userRepository.findByIdWithOptimisticLock(userId)
            .orElseThrow { CoreException(ErrorType.USER_NOT_FOUND) }

        user.status = UserStatus.DELETED
        user.lastLoginAt = LocalDateTime.now()

        userRepository.save(user)

        return true
    }

    @Transactional
    fun hardDelete(userId: Long): Boolean {
        userRepository.deleteById(userId)
        return true
    }

    @Transactional
    fun updateLastLogin(userId: Long): UserInfo {
        val user = userRepository.findById(userId)
            .orElseThrow { CoreException(ErrorType.USER_NOT_FOUND) }

        user.lastLoginAt = LocalDateTime.now()
        val updatedUser = userRepository.save(user)

        return updatedUser.toUserInfo()
    }
}
