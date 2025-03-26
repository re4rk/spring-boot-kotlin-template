package io.dodn.springboot.support.notification.sms

import io.dodn.springboot.client.coolsms.CoolsmsClient
import io.dodn.springboot.client.coolsms.CoolsmsProperties
import io.dodn.springboot.support.notification.sms.model.SmsRequest
import io.dodn.springboot.support.notification.sms.model.SmsResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SmsSender(
    private val coolsmsClient: CoolsmsClient,
    private val coolsmsProperties: CoolsmsProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun sendSms(request: SmsRequest): SmsResponse {
        log.info("Sending SMS to {}", request.to)

        val sender = request.from ?: coolsmsProperties.defaultSender

        val smsResult = if (request.subject != null) {
            coolsmsClient.sendLms(request.to, sender, request.subject, request.content)
        } else {
            coolsmsClient.sendSms(request.to, sender, request.content)
        }

        return SmsResponse(
            messageId = smsResult.messageId,
            success = smsResult.success,
            statusCode = smsResult.statusCode,
            statusMessage = smsResult.statusMessage,
        )
    }
}
