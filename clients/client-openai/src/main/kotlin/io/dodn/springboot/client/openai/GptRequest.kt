package io.dodn.springboot.client.openai

data class GptRequest(
    val model: String = "gpt-3.5-turbo",
    val messages: List<Map<String, String>>,
)
