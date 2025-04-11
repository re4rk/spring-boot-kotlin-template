package io.dodn.springboot.client.openai

import feign.RequestInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@EnableFeignClients
@Configuration
class OpenAiFeignConfig(
    @Value("\${openai.api.key}")
    private val apiKey: String,
) {
    @Bean
    fun requestInterceptor(): RequestInterceptor = RequestInterceptor { template ->
        template.header("Authorization", "Bearer $apiKey")
    }
}
