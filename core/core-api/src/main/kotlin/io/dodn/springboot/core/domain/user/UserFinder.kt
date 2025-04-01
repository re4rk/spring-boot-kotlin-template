package io.dodn.springboot.core.domain.user

import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.core.support.error.ErrorType
import io.dodn.springboot.storage.db.core.user.UserRepository
import io.dodn.springboot.storage.db.core.user.UserStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class UserFinder(
    private val userRepository: UserRepository,
) {
    @Transactional(readOnly = true)
    fun findByEmail(email: String): UserInfo {
        return userRepository.findByEmail(email)
            .map { it.toUserInfo() }
            .orElseThrow { CoreException(ErrorType.USER_NOT_FOUND) }
    }

    @Transactional(readOnly = true)
    fun findByEmailAndStatus(email: String, status: UserStatus): UserInfo {
        val user = findByEmail(email)

        if (user.status != status) {
            throw CoreException(ErrorType.USER_INACTIVE)
        }

        return user
    }

    @Transactional(readOnly = true)
    fun findByStatus(status: UserStatus, page: Int, size: Int): Page<UserInfo> {
        val pageable = PageRequest.of(page, size)
        return userRepository.findByStatus(status, pageable)
            .map { it.toUserInfo() }
    }
}
