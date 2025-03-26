package io.dodn.springboot.support.notification.sms.model

import jakarta.validation.constraints.NotBlank

data class SmsRequest(
    @field:NotBlank
    val to: String,

    val from: String? = null,

    @field:NotBlank
    val content: String,

    val subject: String? = null,
)
