package io.dodn.springboot.core.domain.worry

import io.dodn.springboot.core.domain.worry.counselor.CounselorClient
import io.dodn.springboot.core.domain.worry.counselor.dto.ConversationStep
import io.dodn.springboot.core.domain.worry.counselor.dto.CounselingRequest
import io.dodn.springboot.core.domain.worry.counselor.dto.CounselingResponse
import io.dodn.springboot.core.domain.worry.counselor.dto.EmotionTagRequest
import io.dodn.springboot.core.domain.worry.counselor.dto.EmotionTagResponse
import io.dodn.springboot.core.domain.worry.counselor.dto.SummaryRequest
import org.springframework.stereotype.Component

/**
 * Mapper for converting between domain objects and counselor DTOs
 */
@Component
class CounselorMapper {

    fun toRequest(worry: Worry): CounselingRequest {
        return when (worry.mode) {
            WorryMode.LETTER -> toCounselingRequestForLetter(worry)
            WorryMode.CONVO -> toCounselingRequestForConvo(worry)
        }
    }

    fun toFeedback(response: CounselingResponse, tagResponse: EmotionTagResponse, tone: String): Feedback {
        return Feedback(
            content = response.feedback,
            tone = tone,
            tags = tagResponse.tags,
        )
    }

    fun toSummaryRequest(worry: Worry): SummaryRequest {
        val conversationText = when (worry.mode) {
            WorryMode.LETTER -> worry.content
            WorryMode.CONVO -> buildConversationText(worry.steps)
        }

        return SummaryRequest(fullConversation = conversationText)
    }

    fun toEmotionTagRequest(worry: Worry): EmotionTagRequest {
        val content = when (worry.mode) {
            WorryMode.LETTER -> worry.content
            WorryMode.CONVO -> buildConversationText(worry.steps)
        }

        return EmotionTagRequest(fullConversation = content)
    }

    private fun toCounselingRequestForLetter(worry: Worry): CounselingRequest {
        return CounselingRequest(
            userInput = worry.content,
            emotion = worry.emotion,
            category = worry.category,
            options = worry.options.map { "${it.label}. ${it.text}" },
        )
    }

    private fun toCounselingRequestForConvo(worry: Worry): CounselingRequest {
        // Get the last user message as the current input
        val lastUserStep = worry.steps.lastOrNull { it.role.name.equals("USER", ignoreCase = true) }
        val userInput = lastUserStep?.content ?: ""

        // Convert all steps to conversation history
        val conversationHistory = worry.steps.dropLast(1).map { step ->
            ConversationStep(
                role = step.role.name.lowercase(),
                content = step.content,
            )
        }

        return CounselingRequest(
            userInput = userInput,
            emotion = worry.emotion,
            category = worry.category,
            conversationHistory = conversationHistory,
        )
    }

    private fun buildConversationText(steps: List<WorryStep>): String {
        return steps.joinToString("\n") { step ->
            val rolePrefix = if (step.role.name.equals("USER", ignoreCase = true)) "사용자" else "AI"
            "$rolePrefix: ${step.content}"
        }
    }

    /**
     * Determine the tone of the AI feedback
     */
    fun determineTone(client: CounselorClient, feedback: String): String {
        val toneRequest = CounselingRequest(
            userInput = """
                다음 상담 답변의 어조를 한 단어로 표현해주세요.
                예: 공감적, 위로하는, 격려하는, 질문하는, 분석적, 조언하는 등
                [답변]
                $feedback
            """.trimIndent(),
            emotion = "",
            category = "",
        )

        val toneResponse = client.getCounseling(toneRequest)
        return toneResponse.feedback.trim()
    }
}
