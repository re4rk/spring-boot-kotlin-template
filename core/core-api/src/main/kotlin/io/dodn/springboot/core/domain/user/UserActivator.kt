package io.dodn.springboot.core.domain.user

import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.core.support.error.ErrorType
import io.dodn.springboot.storage.db.core.user.UserRepository
import io.dodn.springboot.storage.db.core.user.UserStatus
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class UserActivator(
    private val userRepository: UserRepository,
) {
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
}
