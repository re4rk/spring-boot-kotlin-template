package io.dodn.springboot.client.openai

import feign.Response
import io.dodn.springboot.client.openai.model.ChatCompletionRequest
import io.dodn.springboot.client.openai.model.ChatCompletionResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(name = "openai-api", url = "\${openai.api.url}")
internal interface OpenAiApi {
    @PostMapping(
        path = ["/chat/completions"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun createChatCompletion(
        @RequestBody request: ChatCompletionRequest,
    ): ChatCompletionResponse

    @PostMapping(
        path = ["/chat/completions"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun createStreamingChatCompletion(
        @RequestBody request: ChatCompletionRequest,
    ): Response
}
