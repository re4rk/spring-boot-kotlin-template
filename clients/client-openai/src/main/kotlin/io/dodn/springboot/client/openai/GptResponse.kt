package io.dodn.springboot.client.openai

import io.dodn.springboot.client.openai.model.Choice

data class GptResponse(
    val choices: List<Choice>,
)
