package io.dodn.springboot.core.api.controller.v1.request

data class UserChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String,
)
