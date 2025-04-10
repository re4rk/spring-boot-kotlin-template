package io.dodn.springboot.core.domain.counseling.counselor

import io.dodn.springboot.client.openai.GptRequest
import io.dodn.springboot.client.openai.OpenAiApiClient
import io.dodn.springboot.core.domain.counseling.counselor.dto.CounselingRequest
import io.dodn.springboot.core.domain.counseling.counselor.dto.CounselingResponse
import io.dodn.springboot.core.domain.counseling.counselor.dto.EmotionTagRequest
import io.dodn.springboot.core.domain.counseling.counselor.dto.EmotionTagResponse
import io.dodn.springboot.core.domain.counseling.counselor.dto.SummaryRequest
import io.dodn.springboot.core.domain.counseling.counselor.dto.SummaryResponse
import org.springframework.stereotype.Component

@Component
class OpenAiCounselorClient(
    private val openAiApiClient: OpenAiApiClient,
) : AiCounselorClient {

    override fun getCounseling(request: CounselingRequest): CounselingResponse {
        val messages = listOf(
            mapOf("role" to "system", "content" to SYSTEM_PROMPT),
            mapOf("role" to "user", "content" to request.userInput),
        )
        val response = openAiApiClient.callGpt(GptRequest(messages = messages))
        return CounselingResponse(response.choices.firstOrNull()?.message?.content ?: "")
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
            mapOf("role" to "system", "content" to SYSTEM_PROMPT),
            mapOf("role" to "user", "content" to summaryPrompt),
        )
        val result = openAiApiClient.callGpt(GptRequest(messages = messages))
        val content = result.choices.firstOrNull()?.message?.content ?: ""
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
            [상담 내용]
            ${request.fullConversation}
        """.trimIndent()
        val messages = listOf(
            mapOf("role" to "system", "content" to SYSTEM_PROMPT),
            mapOf("role" to "user", "content" to tagPrompt),
        )
        val result = openAiApiClient.callGpt(GptRequest(messages = messages))
        val raw = result.choices.firstOrNull()?.message?.content ?: ""
        val tags = raw.split(",", "\n").map { it.trim() }.filter { it.isNotBlank() }
        return EmotionTagResponse(tags)
    }

    companion object {
        private val SYSTEM_PROMPT = """
            당신은 따뜻하고 공감 능력이 뛰어난 AI 고민 상담사입니다. 
            사용자의 고민을 잘 들어주고, 감정을 공감해주며, 부담스럽지 않게 조언을 건네주세요.
            지나치게 딱딱하거나 훈계조가 되지 않도록 하고, 친구처럼 말해주세요.
        """.trimIndent()
    }
}
