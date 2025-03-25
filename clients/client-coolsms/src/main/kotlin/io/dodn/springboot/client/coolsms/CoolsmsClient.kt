package io.dodn.springboot.client.coolsms

import io.dodn.springboot.client.coolsms.dto.SendSmsRequestDto
import io.dodn.springboot.client.coolsms.model.SmsResult
import org.springframework.stereotype.Component

@Component
class CoolsmsClient internal constructor(
    private val coolsmsApi: CoolsmsApi,
    private val coolsmsProperties: CoolsmsProperties,
) {
    fun sendSms(to: String, from: String, text: String): SmsResult {
        val request = SendSmsRequestDto(
            apiKey = coolsmsProperties.apiKey,
            apiSecret = coolsmsProperties.apiSecret,
            to = to,
            from = from,
            text = text,
            type = "SMS"
        )
        return coolsmsApi.sendSms(request).toResult()
    }

    fun sendLms(to: String, from: String, subject: String, text: String): SmsResult {
        val request = SendSmsRequestDto(
            apiKey = coolsmsProperties.apiKey,
            apiSecret = coolsmsProperties.apiSecret,
            to = to,
            from = from,
            text = text,
            subject = subject,
            type = "LMS"
        )
        return coolsmsApi.sendSms(request).toResult()
    }
}