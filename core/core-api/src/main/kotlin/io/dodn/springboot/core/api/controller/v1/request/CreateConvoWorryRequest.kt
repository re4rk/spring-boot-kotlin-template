package io.dodn.springboot.core.api.controller.v1.request

import io.dodn.springboot.core.domain.worry.Worry
import io.dodn.springboot.core.domain.worry.WorryMode
import io.dodn.springboot.core.domain.worry.WorryMessage

data class CreateConvoWorryRequest(
    val emotion: String,
    val category: String,
    val steps: List<StepRequest>,
) {
    fun toWorry(userId: Long): Worry {
        return Worry(
            id = 0L,
            userId = userId,
            mode = WorryMode.CONVO,
            emotion = emotion,
            category = category,
            content = "",
            lastStepOrder = steps.size - 1,
            steps = steps.mapIndexed { index, step ->
                WorryMessage(
                    id = index.toLong(),
                    role = step.role,
                    content = step.content,
                    messageOrder = index,
                )
            },
        )
    }
}
