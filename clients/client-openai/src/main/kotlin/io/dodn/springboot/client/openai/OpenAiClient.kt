package io.dodn.springboot.client.openai

import feign.Response
import io.dodn.springboot.client.openai.model.ChatCompletionRequest
import io.dodn.springboot.client.openai.model.ChatCompletionResponse
import io.dodn.springboot.client.openai.model.Message
import org.springframework.stereotype.Component

@Component
class OpenAiClient internal constructor(
    private val openAiApi: OpenAiApi,
    private val openAiProperties: OpenAiProperties,
) {
    /**
     * 일반 채팅 완성 요청
     */
    fun createChatCompletion(
        messages: List<Message>,
        temperature: Double = 0.7,
        maxTokens: Int? = null,
        topP: Double? = null,
        frequencyPenalty: Double? = null,
        presencePenalty: Double? = null,
    ): ChatCompletionResponse {
        val request = ChatCompletionRequest(
            model = openAiProperties.model,
            messages = messages,
            temperature = temperature,
            maxTokens = maxTokens,
            topP = topP,
            frequencyPenalty = frequencyPenalty,
            presencePenalty = presencePenalty,
            stream = false,
        )

        return openAiApi.createChatCompletion(request)
    }

    /**
     * 스트리밍 채팅 완성 요청
     */
    fun createStreamingChatCompletion(
        messages: List<Message>,
        temperature: Double = 0.7,
        maxTokens: Int? = null,
        topP: Double? = null,
        frequencyPenalty: Double? = null,
        presencePenalty: Double? = null,
    ): Response {
        val request = ChatCompletionRequest(
            model = openAiProperties.model,
            messages = messages,
            temperature = temperature,
            maxTokens = maxTokens,
            topP = topP,
            frequencyPenalty = frequencyPenalty,
            presencePenalty = presencePenalty,
            stream = true,
        )

        return openAiApi.createStreamingChatCompletion(request)
    }

    /**
     * 간단한 질문에 대한 응답 생성 헬퍼 메소드
     */
    fun askQuestion(
        question: String,
        systemMessage: String = "You are a helpful assistant.",
        temperature: Double = 0.7,
    ): String {
        val messages = listOf(
            Message(role = "system", content = systemMessage),
            Message(role = "user", content = question),
        )

        val response = createChatCompletion(messages, temperature)
        return response.choices.firstOrNull()?.message?.content
            ?: throw RuntimeException("No response content received")
    }

    // JSON 응답에서 콘텐츠 추출하는 간단한 방법
    // 실제 구현에서는 Jackson ObjectMapper를 사용하는 것이 좋음
    private fun extractContentFromStreamJson(json: String): String {
        val contentMatch = "\"content\":\"([^\"]*?)\"".toRegex().find(json)
        return contentMatch?.groupValues?.getOrNull(1)?.replace("\\n", "\n")?.replace("\\\"", "\"") ?: ""
    }
}
