package io.dodn.springboot.client.coolsms

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Configuration

@EnableFeignClients
@Configuration
internal class CoolsmsConfig

@ConfigurationProperties(prefix = "coolsms")
data class CoolsmsProperties(
    val apiKey: String,
    val apiSecret: String,
    val defaultSender: String,
)