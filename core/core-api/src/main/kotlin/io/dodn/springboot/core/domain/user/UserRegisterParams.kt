package io.dodn.springboot.core.domain.user

data class UserRegisterParams(
    val email: String,
    val password: String,
    val name: String?,
)
