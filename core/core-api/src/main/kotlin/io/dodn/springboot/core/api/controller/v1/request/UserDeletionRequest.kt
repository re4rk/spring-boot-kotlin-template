package io.dodn.springboot.core.api.controller.v1.request

data class UserDeletionRequest(
    val password: String,
    val reason: String? = null,
)
