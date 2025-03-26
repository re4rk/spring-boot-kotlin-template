package io.dodn.springboot.support.notification.sms.model

data class SmsResponse(
    val messageId: String,
    val success: Boolean,
    val statusCode: String,
    val statusMessage: String,
)
