package io.dodn.springboot.core.domain.user

import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.core.support.error.ErrorType
import io.dodn.springboot.storage.db.core.user.UserRepository
import io.dodn.springboot.storage.db.core.user.UserStatus
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class UserLocker(
    private val userRepository: UserRepository,
) {
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
}
