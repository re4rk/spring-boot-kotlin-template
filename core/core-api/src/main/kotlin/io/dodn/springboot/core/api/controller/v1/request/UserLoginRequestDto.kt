package io.dodn.springboot.core.api.controller.v1.request

data class UserLoginRequestDto(
    val email: String,
    val password: String,
)
