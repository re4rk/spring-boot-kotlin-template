package io.dodn.springboot.core.domain.token

import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.core.support.error.ErrorType
import io.dodn.springboot.storage.db.core.token.RefreshTokenEntity
import io.dodn.springboot.storage.db.core.token.RefreshTokenRepository
import io.dodn.springboot.storage.db.core.user.UserRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class TokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val userRepository: UserRepository,
) {

    @Transactional
    fun createRefreshToken(userEmail: String, token: String, expiresAt: LocalDateTime): String {
        val user = userRepository.findByEmail(userEmail)
            .orElseThrow { CoreException(ErrorType.USER_NOT_FOUND) }

        val refreshToken = RefreshTokenEntity(
            token = token,
            userId = user.id,
            expiresAt = expiresAt,
        )

        refreshTokenRepository.save(refreshToken)
        return token
    }

    @Transactional(readOnly = true)
    fun validateRefreshToken(token: String): String {
        val refreshToken = refreshTokenRepository.findByToken(token)
            .orElseThrow { CoreException(ErrorType.INVALID_CREDENTIALS) }

        if (refreshToken.expiresAt.isBefore(LocalDateTime.now())) {
            throw CoreException(ErrorType.INVALID_CREDENTIALS)
        }

        val user = userRepository.findById(refreshToken.userId)
            .orElseThrow { CoreException(ErrorType.USER_NOT_FOUND) }

        return user.email
    }

    @Transactional
    fun deleteRefreshToken(token: String) {
        val refreshToken = refreshTokenRepository.findByToken(token)
        refreshToken.ifPresent { refreshTokenRepository.delete(it) }
    }

    @Transactional
    fun deleteAllUserRefreshTokens(userEmail: String) {
        val user = userRepository.findByEmail(userEmail)
            .orElseThrow { CoreException(ErrorType.USER_NOT_FOUND) }

        refreshTokenRepository.deleteAllByUserId(user.id)
    }

    @Scheduled(cron = "0 0 0 * * ?") // Run at midnight every day
    @Transactional
    fun cleanupExpiredTokens() {
        refreshTokenRepository.deleteAllExpiredTokens(LocalDateTime.now())
    }
}
