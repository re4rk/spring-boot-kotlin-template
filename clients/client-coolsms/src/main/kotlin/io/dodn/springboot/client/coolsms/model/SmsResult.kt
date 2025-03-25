package io.dodn.springboot.client.coolsms.model

data class SmsResult(
    val messageId: String,
    val success: Boolean,
    val statusCode: String,
    val statusMessage: String,
)