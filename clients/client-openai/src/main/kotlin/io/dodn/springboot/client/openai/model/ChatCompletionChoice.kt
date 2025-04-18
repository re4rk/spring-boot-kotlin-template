package io.dodn.springboot.client.openai.model

import com.fasterxml.jackson.annotation.JsonProperty

data class ChatCompletionChoice(
    val index: Int,
    val message: Message,

    @JsonProperty("finish_reason")
    val finishReason: String?,
)
