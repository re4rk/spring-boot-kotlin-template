package io.dodn.springboot.core.domain.user.password

data class PasswordResetVerifyDto(
    val token: String,
    val newPassword: String,
)
