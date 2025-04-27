package io.dodn.springboot.core.api.controller.v1.request

import io.dodn.springboot.core.domain.worry.Worry
import io.dodn.springboot.core.domain.worry.WorryMode
import io.dodn.springboot.core.domain.worry.WorryMessage

data class CreateConvoWorryRequest(
    val emotion: String,
    val category: String,
    val messages: List<MessageRequest>,
) {
    fun toWorry(userId: Long): Worry {
        return Worry(
            id = 0L,
            userId = userId,
            mode = WorryMode.CONVO,
            emotion = emotion,
            category = category,
            content = "",
            lastMessageOrder = messages.size - 1,
            messages = messages.mapIndexed { index, message ->
                WorryMessage(
                    id = index.toLong(),
                    role = message.role,
                    content = message.content,
                    messageOrder = index,
                )
            },
        )
    }
}
