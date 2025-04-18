package io.dodn.springboot.client.openai

import feign.Response
import io.dodn.springboot.client.ClientOpenAiContextTest
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

class OpenAiClientTest : ClientOpenAiContextTest() {

    private lateinit var openAiApi: OpenAiApi
    private lateinit var openAiClient: OpenAiClient
    private lateinit var openAiProperties: OpenAiProperties

    @BeforeEach
    fun setup() {
        openAiApi = Mockito.mock(OpenAiApi::class.java)
        openAiProperties = OpenAiProperties("gpt-3.5-turbo")
        openAiClient = OpenAiClient(openAiApi, openAiProperties)
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

        Mockito.`when`(openAiApi.createChatCompletion(Mockito.any())).thenReturn(mockResponse)

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

        Mockito.`when`(openAiApi.createChatCompletion(Mockito.any())).thenReturn(mockResponse)

        // when
        val messages = listOf(
            Message(role = "user", content = "Who are you?"),
        )
        val result = openAiClient.createChatCompletion(messages)

        // then
        Assertions.assertThat(result.choices[0].message.content).isEqualTo("I'm an AI assistant.")
    }

    @Test
    fun `createStreamingChatCompletion 스트림 값이 true로 설정되는지 테스트`() {
        // given
        val mockFeign = Mockito.mock(Response::class.java)

        // ChatCompletionRequest 캡처를 위한 설정
        val requestCaptor = ArgumentCaptor.forClass(ChatCompletionRequest::class.java)

        Mockito.`when`(openAiApi.createStreamingChatCompletion(requestCaptor.capture())).thenReturn(mockFeign)

        // when
        val messages = listOf(
            Message(role = "user", content = "Tell me a story"),
        )
        openAiClient.createStreamingChatCompletion(messages)

        // then
        val capturedRequest = requestCaptor.value
        Assertions.assertThat(capturedRequest.stream).isTrue()
        Assertions.assertThat(capturedRequest.model).isEqualTo("gpt-3.5-turbo")
    }
}
