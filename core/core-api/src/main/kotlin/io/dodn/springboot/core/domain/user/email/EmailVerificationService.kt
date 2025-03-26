package io.dodn.springboot.core.domain.user.email

import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.core.support.error.ErrorType
import io.dodn.springboot.storage.db.core.user.email.EmailVerificationTokenEntity
import io.dodn.springboot.storage.db.core.user.email.EmailVerificationTokenRepository
import io.dodn.springboot.storage.db.core.user.UserRepository
import io.dodn.springboot.storage.db.core.user.UserStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.time.LocalDateTime
import java.util.*

@Service
class EmailVerificationService(
    private val userRepository: UserRepository,
    private val verificationTokenRepository: EmailVerificationTokenRepository,
    private val emailSender: EmailSender,
) {

    @Value("\${email.verification.token.expiration:86400}") // 24시간
    private val verificationTokenExpirationSeconds: Long = 86400

    @Transactional
    fun sendVerificationEmail(email: String): Boolean {
        val user = userRepository.findByEmail(email)
            .orElseThrow { CoreException(ErrorType.USER_NOT_FOUND) }

        if (user.status != UserStatus.PENDING_VERIFICATION) {
            throw CoreException(ErrorType.USER_ALREADY_VERIFIED)
        }

        // 기존 토큰 제거
        verificationTokenRepository.deleteAllByUserId(user.id)

        // 새 토큰 생성
        val token = generateSecureToken()
        val expiryTime = LocalDateTime.now().plusSeconds(verificationTokenExpirationSeconds)

        verificationTokenRepository.save(
            EmailVerificationTokenEntity(
                token = token,
                userId = user.id,
                expiresAt = expiryTime,
            ),
        )

        // 이메일 발송
        emailSender.sendVerificationEmail(email = email, verificationLink = "/verify-email?token=$token")

        return true
    }

    @Transactional
    fun verifyEmail(token: String): Boolean {
        val verificationToken = verificationTokenRepository.findByToken(token)
            .orElseThrow { CoreException(ErrorType.INVALID_VERIFICATION_TOKEN) }

        if (verificationToken.expiresAt.isBefore(LocalDateTime.now())) {
            throw CoreException(ErrorType.EXPIRED_VERIFICATION_TOKEN)
        }

        val user = userRepository.findById(verificationToken.userId)
            .orElseThrow { CoreException(ErrorType.USER_NOT_FOUND) }

        // 사용자 상태 변경
        user.status = UserStatus.ACTIVE
        userRepository.save(user)

        // 사용된 토큰 제거
        verificationTokenRepository.delete(verificationToken)

        return true
    }

    private fun generateSecureToken(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    @Scheduled(cron = "0 0 */12 * * *") // 12시간마다 실행
    @Transactional
    fun cleanupExpiredTokens() {
        verificationTokenRepository.deleteAllExpiredTokens(LocalDateTime.now())
    }
}
