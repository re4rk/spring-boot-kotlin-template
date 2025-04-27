package io.dodn.springboot.core.api.controller.v1.response

import io.dodn.springboot.core.domain.worry.Worry
import io.dodn.springboot.core.domain.worry.WorryMode

data class WorryResponse(
    val worryId: Long,
    val mode: WorryMode,
    val emotion: String,
    val category: String,
    val content: String?,
    val steps: List<WorryStepResponse>?,
    val options: List<WorryOptionResponse>?,
) {
    companion object {
        fun from(worry: Worry): WorryResponse {
            return WorryResponse(
                worryId = worry.id,
                mode = worry.mode,
                emotion = worry.emotion,
                category = worry.category,
                content = worry.content,
                steps = worry.steps.takeIf { it.isNotEmpty() }?.map { WorryStepResponse.from(it) },
                options = worry.options.takeIf { it.isNotEmpty() }?.map { WorryOptionResponse.from(it) },
            )
        }
    }
}
