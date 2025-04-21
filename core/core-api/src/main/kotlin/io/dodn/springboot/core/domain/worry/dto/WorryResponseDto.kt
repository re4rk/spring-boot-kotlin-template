package io.dodn.springboot.core.domain.worry.dto

import io.dodn.springboot.core.domain.worry.Worry
import io.dodn.springboot.core.domain.worry.WorryMode

data class WorryResponseDto(
    val worryId: Long,
    val mode: WorryMode,
    val emotion: String,
    val category: String,
    val content: String?,
    val steps: List<WorryStepResponseDto>?,
    val options: List<WorryOptionResponseDto>?,
) {
    companion object {
        fun from(worry: Worry): WorryResponseDto {
            return WorryResponseDto(
                worryId = worry.id!!,
                mode = worry.mode,
                emotion = worry.emotion,
                category = worry.category,
                content = worry.content,
                steps = worry.steps.takeIf { it.isNotEmpty() }?.map { WorryStepResponseDto.from(it) },
                options = worry.options.takeIf { it.isNotEmpty() }?.map { WorryOptionResponseDto.from(it) },
            )
        }
    }
}
