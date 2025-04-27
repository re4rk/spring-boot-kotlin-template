package io.dodn.springboot.core.domain.worry

import io.dodn.springboot.core.domain.worry.counselor.CounselorClient
import io.dodn.springboot.core.domain.worry.counselor.dto.ConversationMessage
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
            WorryMode.LETTER -> buildConversationText(worry.messages)
            WorryMode.CONVO -> buildConversationText(worry.messages)
        }

        return SummaryRequest(fullConversation = conversationText)
    }

    private fun toCounselingRequestForConvo(worry: Worry): CounselingRequest {
        // Get the last user message as the current input
        val worryMessage = worry.messages.lastOrNull { it.role.name.equals("USER", ignoreCase = true) }
        val userInput = worryMessage?.content ?: ""

        // Convert all messages to conversation history
        val messages = worry.messages.dropLast(1).map { message ->
            ConversationMessage(
                role = message.role.name.lowercase(),
                content = message.content,
            )
        }

        return CounselingRequest(
            userInput = userInput,
            emotion = worry.emotion,
            category = worry.category,
            messages = messages,
        )
    }

    private fun buildConversationText(messages: List<WorryMessage>): String {
        return messages.joinToString("\n") { message ->
            val rolePrefix = if (message.role.name.equals("USER", ignoreCase = true)) "사용자" else "AI"
            "$rolePrefix: ${message.content}"
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
