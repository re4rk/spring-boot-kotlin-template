package io.dodn.springboot.core.domain.user.dto

data class UserChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String,
)
