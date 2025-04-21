package io.dodn.springboot.core.api.controller.v1.request

data class PasswordResetVerifyRequest(
    val token: String,
    val newPassword: String,
)
