package io.dodn.springboot.core.domain.user

data class UserDeletionRequest(
    val password: String,
    val reason: String? = null,
)
