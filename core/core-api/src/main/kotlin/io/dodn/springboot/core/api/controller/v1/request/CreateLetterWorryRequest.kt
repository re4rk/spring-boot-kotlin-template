package io.dodn.springboot.core.api.controller.v1.request

import io.dodn.springboot.core.domain.worry.MessageRole
import io.dodn.springboot.core.domain.worry.Worry
import io.dodn.springboot.core.domain.worry.WorryMode
import io.dodn.springboot.core.domain.worry.WorryOption
import io.dodn.springboot.core.domain.worry.WorryMessage

data class CreateLetterWorryRequest(
    val emotion: String,
    val content: String,
    val category: String,
    val options: List<OptionReqeust>? = null,
) {
    fun toWorry(userId: Long): Worry {
        return Worry(
            id = 0L,
            userId = userId,
            mode = WorryMode.LETTER,
            emotion = emotion,
            category = category,
            content = content,
            lastMessageOrder = 0,
            messages = listOf(
                WorryMessage(
                    id = 0L,
                    role = MessageRole.USER,
                    content = content,
                    messageOrder = 0,
                ),
            ),
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
