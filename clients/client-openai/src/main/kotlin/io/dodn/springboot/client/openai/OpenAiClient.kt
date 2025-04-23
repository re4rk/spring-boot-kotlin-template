package io.dodn.springboot.client.openai

import com.fasterxml.jackson.databind.ObjectMapper
import feign.Response
import io.dodn.springboot.client.openai.model.ChatCompletionRequest
import io.dodn.springboot.client.openai.model.ChatCompletionResponse
import io.dodn.springboot.client.openai.model.Message
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

@Component
class OpenAiClient internal constructor(
    private val openAiApi: OpenAiApi,
    private val openAiProperties: OpenAiProperties,
    private val objectMapper: ObjectMapper,
    private val executor: ThreadPoolTaskExecutor,
) {
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

    fun createStreamingChatCompletion(
        messages: List<Message>,
        temperature: Double = 0.7,
        maxTokens: Int? = null,
        topP: Double? = null,
        frequencyPenalty: Double? = null,
        presencePenalty: Double? = null,
        onChunk: (String) -> Unit,
        onComplete: (String) -> Unit,
        onError: (Throwable) -> Unit = { error -> println("Error in streaming completion: ${error.message}") },
    ) {
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

        // 비동기로 처리
        executor.submit {
            var response: Response? = null
            try {
                response = openAiApi.createStreamingChatCompletion(request)
                val reader = BufferedReader(InputStreamReader(response.body().asInputStream(), StandardCharsets.UTF_8))

                // 전체 응답을 구성하기 위한 StringBuilder
                val completeResponse = StringBuilder()

                // 라인별로 스트림 처리
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    // 빈 라인 스킵
                    if (line.isNullOrBlank() || !line!!.startsWith("data:")) {
                        continue
                    }

                    // "data:" 프리픽스 제거
                    val data = line!!.substring(5).trim()

                    // 스트림 종료 표시
                    if (data == "[DONE]") {
                        break
                    }

                    try {
                        // JSON 파싱
                        val jsonNode = objectMapper.readTree(data)
                        val choices = jsonNode.get("choices")

                        // delta 콘텐츠 추출
                        if (choices != null && choices.isArray && choices.size() > 0) {
                            val delta = choices.get(0).get("delta")
                            if (delta != null && delta.has("content")) {
                                val content = delta.get("content").asText()
                                if (content.isNotEmpty()) {
                                    // 콘텐츠 청크 처리
                                    onChunk(content)
                                    completeResponse.append(content)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // JSON 파싱 오류, 무시하고 계속 진행
                        println("Error parsing SSE data: ${e.message}")
                    }
                }

                // 완료 콜백 호출
                onComplete(completeResponse.toString())
            } catch (e: Exception) {
                onError(e)
            } finally {
                // 응답 닫기
                try {
                    response?.body()?.close()
                } catch (e: Exception) {
                    println("Error closing response: ${e.message}")
                }
            }
        }
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
}
