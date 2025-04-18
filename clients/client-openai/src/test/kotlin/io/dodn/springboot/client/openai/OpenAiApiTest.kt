package io.dodn.springboot.client.openai

import feign.Response
import io.dodn.springboot.client.ClientOpenAiContextTest
import io.dodn.springboot.client.openai.model.ChatCompletionRequest
import io.dodn.springboot.client.openai.model.Message
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.test.context.TestConstructor
import java.io.BufferedReader
import java.io.InputStreamReader

@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class OpenAiApiTest internal constructor(
    private val openAiApi: OpenAiApi,
) : ClientOpenAiContextTest() {

    @Test
    fun `OpenAI API 호출을 성공한다`() {
        // given
        val request = ChatCompletionRequest(
            model = "gpt-3.5-turbo",
            messages = listOf(
                Message(role = "system", content = "You are a helpful assistant."),
                Message(role = "user", content = "Hello!"),
            ),
        )

        // when & then
        assertDoesNotThrow {
            val response = openAiApi.createChatCompletion(request)

            println("Response ID: ${response.id}")
            println("Response Model: ${response.model}")
            println("Response Choices: ${response.choices}")
            println("Response Usage: ${response.usage}")
            assert(response.choices.isNotEmpty()) { "Response should contain at least one choice." }
        }
    }

    @Test
    fun `OpenAI 스트리밍 API 호출을 성공한다`() {
        // given
        val request = ChatCompletionRequest(
            model = "gpt-3.5-turbo",
            messages = listOf(
                Message(role = "system", content = "You are a helpful assistant."),
                Message(role = "user", content = "Hello!"),
            ),
            stream = true,
        )

        // when & then
        assertDoesNotThrow {
            val response: Response = openAiApi.createStreamingChatCompletion(request)

            response.use { res ->
                val reader = BufferedReader(InputStreamReader(res.body().asInputStream()))
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    println(line)
                }
            }
            response.close()
        }
    }
}
