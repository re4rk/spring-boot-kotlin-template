package io.dodn.springboot.client.openai

import com.fasterxml.jackson.databind.ObjectMapper
import feign.Response
import io.dodn.springboot.client.ClientOpenAiDevelopTest
import io.dodn.springboot.client.openai.model.ChatCompletionChoice
import io.dodn.springboot.client.openai.model.ChatCompletionRequest
import io.dodn.springboot.client.openai.model.ChatCompletionResponse
import io.dodn.springboot.client.openai.model.Message
import io.dodn.springboot.client.openai.model.Usage
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

class OpenAiClientTest : ClientOpenAiDevelopTest() {

    private lateinit var openAiApi: OpenAiApi
    private lateinit var openAiClient: OpenAiClient
    private lateinit var openAiProperties: OpenAiProperties
    private lateinit var objectMapper: ObjectMapper
    private lateinit var executor: ThreadPoolTaskExecutor

    @BeforeEach
    fun setup() {
        openAiApi = mock(OpenAiApi::class.java)
        openAiProperties = OpenAiProperties("gpt-3.5-turbo")
        objectMapper = ObjectMapper()
        executor = ThreadPoolTaskExecutor()
        openAiClient = OpenAiClient(openAiApi, openAiProperties, objectMapper, executor)
    }

    @Test
    fun `askQuestion 메소드 성공 테스트`() {
        // given
        val mockResponse = ChatCompletionResponse(
            id = "mock-id",
            model = "gpt-3.5-turbo",
            choices = listOf(
                ChatCompletionChoice(
                    index = 0,
                    message = Message(role = "assistant", content = "Hello! How can I help you today?"),
                    finishReason = "stop",
                ),
            ),
            usage = Usage(
                promptTokens = 10,
                completionTokens = 10,
                totalTokens = 20,
            ),
        )

        `when`(openAiApi.createChatCompletion(any())).thenReturn(mockResponse)

        // when
        val result = openAiClient.askQuestion("Hello!")

        // then
        Assertions.assertThat(result).isEqualTo("Hello! How can I help you today?")
    }

    @Test
    fun `createChatCompletion 메소드 성공 테스트`() {
        // given
        val mockResponse = ChatCompletionResponse(
            id = "mock-id",
            model = "gpt-3.5-turbo",
            choices = listOf(
                ChatCompletionChoice(
                    index = 0,
                    message = Message(role = "assistant", content = "I'm an AI assistant."),
                    finishReason = "stop",
                ),
            ),
            usage = Usage(
                promptTokens = 8,
                completionTokens = 8,
                totalTokens = 16,
            ),
        )

        `when`(openAiApi.createChatCompletion(any())).thenReturn(mockResponse)

        // when
        val messages = listOf(
            Message(role = "user", content = "Who are you?"),
        )
        val result = openAiClient.createChatCompletion(messages)

        // then
        Assertions.assertThat(result.choices[0].message.content).isEqualTo("I'm an AI assistant.")
    }

    private fun <T> any(): T {
        Mockito.any<T>()
        return null as T
    }
}
