package io.dodn.springboot.core.domain.worry.dto

import io.dodn.springboot.core.domain.worry.Worry
import io.dodn.springboot.core.domain.worry.WorryMode
import io.dodn.springboot.core.domain.worry.WorryOption

data class CreateLetterWorryRequestDto(
    val userId: Long,
    val emotion: String,
    val content: String,
    val category: String,
    val options: List<OptionDto>? = null,
) {
    fun toWorry(): Worry {
        return Worry(
            id = 0L,
            userId = userId,
            mode = WorryMode.LETTER,
            emotion = emotion,
            category = category,
            content = content,
            options = options?.mapIndexed { index, option ->
                WorryOption(
                    id = index.toLong(),
                    label = ('A' + index).toString(),
                    text = option.text,
                )
            } ?: emptyList(),
        )
    }
}
