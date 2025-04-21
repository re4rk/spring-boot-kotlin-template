package io.dodn.springboot.core.api.controller.v1.request

data class UserChangePasswordRequestDto(
    val oldPassword: String,
    val newPassword: String,
)
