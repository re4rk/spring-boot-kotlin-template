package io.dodn.springboot.core.domain.worry

import io.dodn.springboot.core.domain.worry.counselor.CounselorClient
import io.dodn.springboot.core.domain.worry.counselor.dto.ConversationStep
import io.dodn.springboot.core.domain.worry.counselor.dto.CounselingRequest
import io.dodn.springboot.core.domain.worry.counselor.dto.SummaryRequest
import org.springframework.stereotype.Component

/**
 * Mapper for converting between domain objects and counselor DTOs
 */
@Component
class CounselorMapper {

    fun toRequest(worry: Worry): CounselingRequest {
        return when (worry.mode) {
            WorryMode.LETTER -> toCounselingRequestForConvo(worry)
            WorryMode.CONVO -> toCounselingRequestForConvo(worry)
        }
    }

    fun toSummaryRequest(worry: Worry): SummaryRequest {
        val conversationText = when (worry.mode) {
            WorryMode.LETTER -> buildConversationText(worry.steps)
            WorryMode.CONVO -> buildConversationText(worry.steps)
        }

        return SummaryRequest(fullConversation = conversationText)
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

    private fun buildConversationText(steps: List<WorryMessage>): String {
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
                [응답 예시]
                공감적, 위로하는
            """.trimIndent(),
            emotion = "",
            category = "",
        )

        val toneResponse = client.getCounseling(toneRequest)
        return toneResponse.feedback.trim()
    }
}
