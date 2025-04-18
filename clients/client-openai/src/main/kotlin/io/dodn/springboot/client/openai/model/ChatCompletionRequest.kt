package io.dodn.springboot.client.openai.model

import com.fasterxml.jackson.annotation.JsonProperty

data class ChatCompletionRequest(
    val model: String,
    val messages: List<Message>,
    val temperature: Double? = null,

    @JsonProperty("max_tokens")
    val maxTokens: Int? = null,

    @JsonProperty("top_p")
    val topP: Double? = null,

    @JsonProperty("frequency_penalty")
    val frequencyPenalty: Double? = null,

    @JsonProperty("presence_penalty")
    val presencePenalty: Double? = null,

    val stop: List<String>? = null,
    val stream: Boolean? = null,
)
