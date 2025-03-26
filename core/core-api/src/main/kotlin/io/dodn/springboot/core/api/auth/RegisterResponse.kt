package io.dodn.springboot.core.api.auth

import io.dodn.springboot.core.domain.user.UserInfo

data class RegisterResponse(
    val user: UserInfo,
)
