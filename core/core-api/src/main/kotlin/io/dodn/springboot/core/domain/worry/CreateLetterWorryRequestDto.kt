package io.dodn.springboot.core.domain.worry

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

data class OptionDto(
    val text: String,
)

data class StepDto(
    val role: StepRole,
    val content: String,
)

data class CreateAiFeedbackRequestDto(
    val feedback: String,
    val tone: String? = null,
    val tags: List<String>? = null,
)

data class ShareWorryRequestDto(
    val tags: List<String>,
    val mode: WorryMode,
)
