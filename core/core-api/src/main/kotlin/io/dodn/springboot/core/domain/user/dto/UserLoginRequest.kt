package io.dodn.springboot.core.domain.user.dto

data class UserLoginRequest(
    val email: String,
    val password: String,
)
