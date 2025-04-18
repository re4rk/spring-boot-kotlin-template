package io.dodn.springboot.client.openai.model

data class ChatCompletionResponse(
    val id: String,
    val model: String,
    val choices: List<ChatCompletionChoice>,
    val usage: Usage,
)
