package io.dodn.springboot.core.domain.user.password

import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.core.support.error.ErrorType
import io.dodn.springboot.storage.db.core.user.password.PasswordHistoryEntity
import io.dodn.springboot.storage.db.core.user.password.PasswordHistoryRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.regex.Pattern

@Service
class PasswordPolicy(
    private val passwordHistoryRepository: PasswordHistoryRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    @Value("\${security.password.min-length:8}")
    private val minPasswordLength: Int = 8

    @Value("\${security.password.require-digit:true}")
    private val requireDigit: Boolean = true

    @Value("\${security.password.require-lowercase:true}")
    private val requireLowercase: Boolean = true

    @Value("\${security.password.require-uppercase:true}")
    private val requireUppercase: Boolean = true

    @Value("\${security.password.require-special:true}")
    private val requireSpecial: Boolean = true

    fun validatePasswordStrength(password: String): Map<String, Boolean> {
        val validations = mutableMapOf<String, Boolean>()

        validations["length"] = password.length >= minPasswordLength
        validations["digit"] = !requireDigit || DIGIT_PATTERN.matcher(password).matches()
        validations["lowercase"] = !requireLowercase || LOWERCASE_PATTERN.matcher(password).matches()
        validations["uppercase"] = !requireUppercase || UPPERCASE_PATTERN.matcher(password).matches()
        validations["special"] = !requireSpecial || SPECIAL_CHAR_PATTERN.matcher(password).matches()

        return validations
    }

    fun isValidPassword(password: String): Boolean {
        val validations = validatePasswordStrength(password)
        return validations.values.all { it }
    }

    @Value("\${security.password.history-count:5}")
    private val passwordHistoryCount: Int = 5

    @Transactional(readOnly = true)
    fun isPasswordReused(userId: Long, newPassword: String): Boolean {
        val pageable = PageRequest.of(0, passwordHistoryCount)
        val recentPasswords = passwordHistoryRepository.findRecentPasswordsByUserId(userId, pageable)

        return recentPasswords.any { passwordEncoder.matches(newPassword, it.password) }
    }

    @Transactional
    fun addPasswordToHistory(userId: Long, encodedPassword: String) {
        val passwordHistory = PasswordHistoryEntity(
            userId = userId,
            password = encodedPassword,
        )

        passwordHistoryRepository.save(passwordHistory)
    }

    @Transactional
    fun validateAndEncodePassword(userId: Long, password: String): String {
        if (!isValidPassword(password)) {
            throw CoreException(ErrorType.WEAK_PASSWORD)
        }

        if (isPasswordReused(userId, password)) {
            throw CoreException(ErrorType.PASSWORD_REUSED)
        }

        val encodedPassword = passwordEncoder.encode(password)

        return encodedPassword
    }

    companion object {
        private val DIGIT_PATTERN = Pattern.compile(".*\\d.*")
        private val LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*")
        private val UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*")
        private val SPECIAL_CHAR_PATTERN = Pattern.compile(".*[!@#$%^&*(),.?\":{}|<>].*")
    }
}
