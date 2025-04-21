package io.dodn.springboot.core.api.controller.v1.response

import io.dodn.springboot.core.domain.worry.WorryOption

data class WorryOptionResponse(
    val label: String,
    val text: String,
) {
    companion object {
        fun from(option: WorryOption): WorryOptionResponse {
            return WorryOptionResponse(
                label = option.label,
                text = option.text,
            )
        }
    }
}
