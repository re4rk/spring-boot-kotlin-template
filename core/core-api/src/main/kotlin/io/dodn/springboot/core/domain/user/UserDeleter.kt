package io.dodn.springboot.core.domain.user

import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.core.support.error.ErrorType
import io.dodn.springboot.storage.db.core.user.UserRepository
import io.dodn.springboot.storage.db.core.user.UserStatus
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class UserDeleter(
    private val userRepository: UserRepository,
) {
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
}
