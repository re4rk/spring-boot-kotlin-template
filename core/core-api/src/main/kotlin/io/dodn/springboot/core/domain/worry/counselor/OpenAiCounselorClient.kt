package io.dodn.springboot.core.domain.worry.counselor

import io.dodn.springboot.client.openai.OpenAiClient
import io.dodn.springboot.client.openai.model.Message
import io.dodn.springboot.core.domain.worry.counselor.dto.CounselingRequest
import io.dodn.springboot.core.domain.worry.counselor.dto.CounselingResponse
import io.dodn.springboot.core.domain.worry.counselor.dto.EmotionTagRequest
import io.dodn.springboot.core.domain.worry.counselor.dto.EmotionTagResponse
import io.dodn.springboot.core.domain.worry.counselor.dto.SummaryRequest
import io.dodn.springboot.core.domain.worry.counselor.dto.SummaryResponse
import org.springframework.stereotype.Component

@Component
class OpenAiCounselorClient(
    private val openAiClient: OpenAiClient,
) : CounselorClient {

    override fun getCounseling(request: CounselingRequest): CounselingResponse {
        val promptContent = if (request.messages.isEmpty()) {
            buildLetterPrompt(request)
        } else {
            buildConvoPrompt(request)
        }

        val messages = listOf(
            Message(role = "system", content = SYSTEM_PROMPT),
            Message(role = "user", content = promptContent),
        )

        val feedback = openAiClient.createChatCompletion(messages)
            .choices
            .firstOrNull()
            ?.message
            ?.content ?: "죄송합니다, 답변을 생성하는 데 문제가 있었습니다."

        return CounselingResponse(feedback)
    }

    override fun createStreamingChatCompletion(
        request: CounselingRequest,
        onChunk: (String) -> Unit,
        onComplete: (String) -> Unit,
    ) {
        val promptContent = if (request.messages.isEmpty()) {
            buildLetterPrompt(request)
        } else {
            buildConvoPrompt(request)
        }

        val messages = listOf(
            Message(role = "system", content = SYSTEM_PROMPT),
            Message(role = "user", content = promptContent),
        )

        // 개선된 OpenAiClient의 콜백 기반 메서드 사용
        openAiClient.createStreamingChatCompletion(
            messages = messages,
            onChunk = onChunk,
            onComplete = onComplete,
            onError = { error ->
                println("Error in counseling streaming completion: ${error.message}")
                // 에러 발생 시 완료 콜백에 기본 메시지 전달
                onComplete("죄송합니다, 답변을 생성하는 중에 오류가 발생했습니다.")
            },
        )
    }

    override fun summarizeConversation(request: SummaryRequest): SummaryResponse {
        val summaryPrompt = """
            다음은 사용자의 고민 내용과 AI의 상담 내용입니다.
            이 내용을 2~3줄 이내로 요약해 주세요. 감정 중심으로 요약해 주세요.
            그리고 공감형 제목도 하나 생성해 주세요.
            [상담 내용]
            ${request.fullConversation}
        """.trimIndent()

        val messages = listOf(
            Message(role = "system", content = SYSTEM_PROMPT),
            Message(role = "user", content = summaryPrompt),
        )

        val content = openAiClient.createChatCompletion(messages)
            .choices
            .firstOrNull()
            ?.message
            ?.content ?: ""

        val parts = content.split("\n")

        return SummaryResponse(
            summary = parts.getOrNull(0) ?: "요약 없음",
            title = parts.getOrNull(1) ?: "제목 없음",
        )
    }

    override fun extractEmotionTags(request: EmotionTagRequest): EmotionTagResponse {
        val tagPrompt = """
            다음 상담 내용을 보고, 사용자가 느끼는 주요 감정을 2~3개 추출해 주세요. 
            감정은 '슬픔', '불안', '분노', '후회', '무기력', '희망' 등의 단어로 표현해주세요.
            # 태그 형식으로 반환해주세요. 예를 들어: #슬픔 #불안 #희망
            [상담 내용]
            ${request.fullConversation}
        """.trimIndent()

        val messages = listOf(
            Message(role = "system", content = SYSTEM_PROMPT),
            Message(role = "user", content = tagPrompt),
        )

        val raw = openAiClient.createChatCompletion(messages)
            .choices
            .firstOrNull()
            ?.message
            ?.content ?: ""

        // Extract tags, handling both comma-separated and hashtag format
        val tags = raw.split(",", " ", "\n")
            .map { it.trim().removePrefix("#") }
            .filter { it.isNotBlank() }
            .map { "#$it" } // Add hashtag prefix

        return EmotionTagResponse(tags)
    }

    /**
     * Build prompt for letter-mode requests
     */
    private fun buildLetterPrompt(request: CounselingRequest): String {
        val optionsText = if (request.options.isNotEmpty()) {
            "\n사용자가 고려 중인 선택지:\n" +
                request.options.mapIndexed { index, text -> "${('A' + index)}. $text" }.joinToString("\n")
        } else {
            ""
        }

        return """
            다음은 사용자의 고민입니다. 공감하고 도움이 되는 답변을 해주세요.
            
            감정: ${request.emotion}
            카테고리: ${request.category}
            
            고민:
            ${request.userInput}
            $optionsText
        """.trimIndent()
    }

    /**
     * Build prompt for conversation-mode requests
     */
    private fun buildConvoPrompt(request: CounselingRequest): String {
        val conversationText = request.messages.joinToString("\n") { message ->
            val rolePrefix = if (message.role == "user") "사용자" else "AI"
            "$rolePrefix: ${message.content}"
        }

        return """
            다음은 사용자와의 대화입니다. 마지막 메시지에 대한 공감적인 답변을 해주세요.
            
            감정: ${request.emotion}
            카테고리: ${request.category}
            
            대화 내용:
            $conversationText
            사용자: ${request.userInput}
        """.trimIndent()
    }

    companion object {
        private val SYSTEM_PROMPT = """
            당신은 따뜻하고 공감 능력이 뛰어난 AI 고민 상담사입니다. 
            사용자의 고민을 잘 들어주고, 감정을 공감해주며, 부담스럽지 않게 조언을 건네주세요.
            지나치게 딱딱하거나 훈계조가 되지 않도록 하고, 친구처럼 말해주세요.
        """.trimIndent()
    }
}
