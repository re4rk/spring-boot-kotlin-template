package io.dodn.springboot.core.domain.worry

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

data class WorryStepResponseDto(
    val role: String,
    val content: String,
) {
    companion object {
        fun from(step: WorryStep): WorryStepResponseDto {
            return WorryStepResponseDto(
                role = step.role.name.lowercase(),
                content = step.content,
            )
        }
    }
}

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

data class AiFeedbackResponseDto(
    val feedback: String,
    val tone: String?,
    val tags: List<String>,
) {
    companion object {
        fun from(aiFeedback: AiFeedback): AiFeedbackResponseDto {
            return AiFeedbackResponseDto(
                feedback = aiFeedback.feedback,
                tone = aiFeedback.tone,
                tags = aiFeedback.tags,
            )
        }
    }
}

data class FeedResponseDto(
    val feedId: Long,
    val worry: WorryResponseDto,
    val feedback: AiFeedbackResponseDto,
    val empathyCount: Long,
) {
    companion object {
        fun from(feed: Feed): FeedResponseDto {
            return FeedResponseDto(
                feedId = feed.id,
                worry = WorryResponseDto.from(feed.worry),
                feedback = AiFeedbackResponseDto.from(feed.feedback),
                empathyCount = feed.empathyCount,
            )
        }
    }
}

data class FeedSummaryResponseDto(
    val feedId: Long,
    val emotion: String,
    val tags: List<String>,
    val summary: String,
) {
    companion object {
        fun from(feed: Feed): FeedSummaryResponseDto {
            val summary = when (feed.worry.mode) {
                WorryMode.LETTER -> "${feed.worry.content.take(20)}... 고민에 AI가 답변했어요."
                WorryMode.CONVO -> "대화형 고민에 AI가 공감해주었어요."
            }

            return FeedSummaryResponseDto(
                feedId = feed.id,
                emotion = feed.worry.emotion,
                tags = feed.feedback.tags,
                summary = summary,
            )
        }
    }
}

data class EmpathyResponseDto(
    val status: String,
    val count: Long,
)
