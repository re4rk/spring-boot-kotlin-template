package io.dodn.springboot.core.domain.user.password

import io.dodn.springboot.core.domain.user.email.EmailSender
import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.core.support.error.ErrorType
import io.dodn.springboot.storage.db.core.user.UserRepository
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
class PasswordResetService(
    private val userRepository: UserRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val passwordPolicy: PasswordPolicy,
    private val emailSender: EmailSender,
) {

    @Value("\${password.reset.token.expiration:1800}") // 30분
    private val resetTokenExpirationSeconds: Long = 1800

    @Transactional
    fun requestPasswordReset(requestDto: PasswordResetRequestDto): Boolean {
        val user = userRepository.findByEmail(requestDto.email)
            .orElseThrow { CoreException(ErrorType.USER_NOT_FOUND) }

        // 기존 토큰 제거
        passwordResetTokenRepository.deleteAllByUserId(user.id)

        // 새 토큰 생성
        val token = generateSecureToken()
        val expiryTime = LocalDateTime.now().plusSeconds(resetTokenExpirationSeconds)

        passwordResetTokenRepository.save(
            PasswordResetTokenEntity(
                token = token,
                userId = user.id,
                expiresAt = expiryTime,
            ),
        )

        // 이메일 발송
        val resetLink = "/reset-password?token=$token"
        emailSender.sendPasswordResetEmail(user.email, resetLink)

        return true
    }

    @Transactional
    fun resetPassword(resetDto: PasswordResetVerifyDto): Boolean {
        val resetToken = passwordResetTokenRepository.findByToken(resetDto.token)
            .orElseThrow { CoreException(ErrorType.INVALID_RESET_TOKEN) }

        if (resetToken.expiresAt.isBefore(LocalDateTime.now())) {
            throw CoreException(ErrorType.EXPIRED_RESET_TOKEN)
        }

        val user = userRepository.findById(resetToken.userId)
            .orElseThrow { CoreException(ErrorType.USER_NOT_FOUND) }

        // 비밀번호 정책 검증 및 인코딩 (이력 체크 포함)
        val encodedPassword = passwordPolicy.validateAndEncodePassword(
            user.id,
            resetDto.newPassword,
        )

        // 비밀번호 업데이트
        user.password = encodedPassword
        userRepository.save(user)

        // 사용한 토큰 제거
        passwordResetTokenRepository.delete(resetToken)

        return true
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
