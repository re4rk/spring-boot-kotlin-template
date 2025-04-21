package io.dodn.springboot.core.api.controller.v1.request

import io.dodn.springboot.core.domain.worry.Worry
import io.dodn.springboot.core.domain.worry.WorryMode
import io.dodn.springboot.core.domain.worry.WorryStep

data class CreateConvoWorryRequestDto(
    val userId: Long,
    val emotion: String,
    val category: String,
    val steps: List<StepDto>,
) {
    fun toWorry(): Worry {
        return Worry(
            id = 0L,
            userId = userId,
            mode = WorryMode.CONVO,
            emotion = emotion,
            category = category,
            content = "",
            steps = steps.mapIndexed { index, step ->
                WorryStep(
                    id = index.toLong(),
                    role = step.role,
                    content = step.content,
                    stepOrder = index,
                )
            },
        )
    }
}
