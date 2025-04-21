package io.dodn.springboot.core.api.controller.v1.request

import io.dodn.springboot.core.domain.worry.StepRole

data class StepDto(
    val role: StepRole,
    val content: String,
)
