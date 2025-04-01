package io.dodn.springboot.core.domain.user.password

import io.dodn.springboot.core.domain.user.email.EmailSender
import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.core.support.error.ErrorType
import io.dodn.springboot.storage.db.core.user.password.PasswordResetTokenEntity
import io.dodn.springboot.storage.db.core.user.password.PasswordResetTokenRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.time.LocalDateTime
import java.util.Base64

@Service
class PasswordResetManager(
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val emailSender: EmailSender,
) {

    @Value("\${password.reset.token.expiration:1800}") // 30분
    private val resetTokenExpirationSeconds: Long = 1800

    @Transactional
    fun requestPasswordReset(id: Long, email: String): Boolean {
        // 기존 토큰 제거
        passwordResetTokenRepository.deleteAllByUserId(id)

        // 새 토큰 생성
        val token = generateSecureToken()
        val expiryTime = LocalDateTime.now().plusSeconds(resetTokenExpirationSeconds)

        passwordResetTokenRepository.save(
            PasswordResetTokenEntity(
                token = token,
                userId = id,
                expiresAt = expiryTime,
            ),
        )

        // 이메일 발송
        val resetLink = "/reset-password?token=$token"
        emailSender.sendPasswordResetEmail(email, resetLink)

        return true
    }

    @Transactional
    fun verifyPasswordResetToken(token: String): PasswordResetVerifyResult {
        val resetToken = passwordResetTokenRepository.findByToken(token)
            .orElseThrow { CoreException(ErrorType.INVALID_RESET_TOKEN) }

        if (resetToken.usedAt != null) {
            throw CoreException(ErrorType.USED_RESET_TOKEN)
        }

        if (resetToken.expiresAt.isBefore(LocalDateTime.now())) {
            throw CoreException(ErrorType.EXPIRED_RESET_TOKEN)
        }

        resetToken.usedAt = LocalDateTime.now()

        return PasswordResetVerifyResult(true, resetToken.userId)
    }

    private fun generateSecureToken(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    @Scheduled(cron = "0 0 * * * *") // 매시간 실행
    @Transactional
    fun cleanupExpiredTokens() {
        passwordResetTokenRepository.deleteAllExpiredTokens(LocalDateTime.now())
    }
}
