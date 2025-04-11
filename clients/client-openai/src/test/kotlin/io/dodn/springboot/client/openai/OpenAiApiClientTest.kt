package io.dodn.springboot.client.openai

import feign.RetryableException
import io.dodn.springboot.client.ClientOpenAiContextTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OpenAiApiClientTest(
    private val openAiApiClient: OpenAiApiClient,
) : ClientOpenAiContextTest() {

    @Test
    fun `OpenAI API 호출 실패시 RetryableException 발생 테스트`() {
        // given
        val request = GptRequest(
            model = "gpt-3.5-turbo",
            messages = listOf(
                mapOf("role" to "system", "content" to "You are a helpful assistant."),
                mapOf("role" to "user", "content" to "Hello!"),
            ),
        )

        // when & then
        try {
            openAiApiClient.callGpt(request)
        } catch (e: Exception) {
            assertThat(e).isExactlyInstanceOf(RetryableException::class.java)
        }
    }
}
