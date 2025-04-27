package io.dodn.springboot.core.api.controller.v1.request

import io.dodn.springboot.core.domain.worry.MeesageRole

data class MessageRequest(
    val role: MeesageRole,
    val content: String,
)
