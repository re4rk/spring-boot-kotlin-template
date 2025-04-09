package io.dodn.springboot.client.openai

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(name = "openai-api", url = "https://api.openai.com/v1", configuration = [OpenAiFeignConfig::class])
interface OpenAiApiClient {
    @PostMapping(value = ["/chat/completions"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun callGpt(@RequestBody request: GptRequest): GptResponse
}
