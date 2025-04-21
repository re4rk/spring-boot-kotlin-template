package io.dodn.springboot.core.api.controller.v1.request

data class UserLoginRequest(
    val email: String,
    val password: String,
)
