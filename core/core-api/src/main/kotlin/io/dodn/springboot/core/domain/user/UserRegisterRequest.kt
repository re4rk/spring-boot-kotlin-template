package io.dodn.springboot.core.domain.user

data class UserRegisterRequest(
    val email: String,
    val password: String,
    val name: String?,
)
