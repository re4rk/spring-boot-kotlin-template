package io.dodn.springboot.core.domain.user.dto

data class UserDeletionRequestDto(
    val password: String,
    val reason: String? = null,
)
