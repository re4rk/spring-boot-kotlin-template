package io.dodn.springboot.core.domain.user.email

import org.springframework.stereotype.Component

@Component
class EmailSender {
    fun sendVerificationEmail(email: String, verificationLink: String) {
        println("Send verification email to $email with link $verificationLink")
    }
}
