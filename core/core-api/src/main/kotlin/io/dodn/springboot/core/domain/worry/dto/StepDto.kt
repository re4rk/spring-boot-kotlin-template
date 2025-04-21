package io.dodn.springboot.core.domain.worry.dto

import io.dodn.springboot.core.domain.worry.StepRole

data class StepDto(
    val role: StepRole,
    val content: String,
)
