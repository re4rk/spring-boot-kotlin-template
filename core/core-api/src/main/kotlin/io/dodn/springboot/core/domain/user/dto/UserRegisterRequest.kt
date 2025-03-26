package io.dodn.springboot.core.domain.user.dto

data class UserRegisterRequest(
    val email: String,
    val password: String,
    val name: String?,
)
