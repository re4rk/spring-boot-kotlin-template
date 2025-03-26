package io.dodn.springboot.core.domain.user.dto

import io.dodn.springboot.core.domain.user.UserInfo

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserInfo,
)
