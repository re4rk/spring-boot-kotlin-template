package io.dodn.springboot.client.coolsms.dto

import io.dodn.springboot.client.coolsms.model.SmsResult

internal data class SendSmsResponseDto(
    val groupId: String,
    val messageId: String,
    val accountId: String,
    val statusCode: String,
    val statusMessage: String,
    val to: String,
    val from: String,
    val type: String,
    val country: String,
) {
    fun toResult(): SmsResult {
        val success = statusCode == "200" || statusCode == "2000" || statusCode == "success"
        return SmsResult(
            messageId = messageId,
            success = success,
            statusCode = statusCode,
            statusMessage = statusMessage,
        )
    }
}
