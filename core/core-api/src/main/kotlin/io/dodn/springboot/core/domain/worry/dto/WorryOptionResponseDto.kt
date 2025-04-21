package io.dodn.springboot.core.domain.worry.dto

import io.dodn.springboot.core.domain.worry.WorryOption

data class WorryOptionResponseDto(
    val label: String,
    val text: String,
) {
    companion object {
        fun from(option: WorryOption): WorryOptionResponseDto {
            return WorryOptionResponseDto(
                label = option.label,
                text = option.text,
            )
        }
    }
}
