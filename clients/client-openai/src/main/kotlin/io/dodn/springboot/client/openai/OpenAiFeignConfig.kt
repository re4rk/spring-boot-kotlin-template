package io.dodn.springboot.client.openai

import feign.RequestInterceptor
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@EnableFeignClients
@Configuration
class OpenAiFeignConfig {
    @Bean
    fun requestInterceptor(): RequestInterceptor = RequestInterceptor { template ->
        template.header("Authorization", "Bearer YOUR_OPENAI_API_KEY")
    }
}
