package io.dodn.springboot.client.openai

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

// OpenAI 설정 속성 클래스
@Component
class OpenAiProperties(
    @Value("\${openai.api.model:gpt-4}")
    val model: String,
)
