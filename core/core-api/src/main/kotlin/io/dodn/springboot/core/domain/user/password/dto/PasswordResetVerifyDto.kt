package io.dodn.springboot.core.domain.user.password.dto

data class PasswordResetVerifyDto(
    val token: String,
    val newPassword: String,
)
