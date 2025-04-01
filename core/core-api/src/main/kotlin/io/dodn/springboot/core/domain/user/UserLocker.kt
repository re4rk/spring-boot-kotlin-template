package io.dodn.springboot.core.domain.user

import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.core.support.error.ErrorType
import io.dodn.springboot.storage.db.core.user.UserRepository
import io.dodn.springboot.storage.db.core.user.UserStatus
import org.springframework.stereotype.Component

@Component
class UserLocker(
    private val userRepository: UserRepository,
) {
    fun lock(userId: Long): Boolean {
        val user = userRepository.findById(userId)
            .orElseThrow { CoreException(ErrorType.USER_NOT_FOUND) }

        if (user.status == UserStatus.LOCKED) {
            throw CoreException(ErrorType.USER_ALREADY_LOCKED)
        }

        user.status = UserStatus.LOCKED
        userRepository.save(user)
        return true
    }

    fun unlock(userId: Long): Boolean {
        val user = userRepository.findById(userId)
            .orElseThrow { CoreException(ErrorType.USER_NOT_FOUND) }

        if (user.status != UserStatus.LOCKED) {
            throw CoreException(ErrorType.USER_NOT_LOCKED)
        }

        user.status = UserStatus.ACTIVE
        userRepository.save(user)
        return true
    }
}
