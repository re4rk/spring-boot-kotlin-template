package io.dodn.springboot.core.domain.notification

import io.dodn.springboot.support.notification.sms.SmsSender
import io.dodn.springboot.support.notification.sms.model.SmsRequest
import io.dodn.springboot.support.notification.sms.model.SmsResponse
import org.springframework.stereotype.Component

@Component
class NotificationSender(
    private val smsSender: SmsSender,
) {
    fun sendSmsNotification(phoneNumber: String, message: String): SmsResponse = smsSender.sendSms(
        SmsRequest(
            to = phoneNumber,
            content = message,
        ),
    )

    fun sendLmsNotification(phoneNumber: String, subject: String, message: String): SmsResponse = smsSender.sendSms(
        SmsRequest(
            to = phoneNumber,
            content = message,
            subject = subject,
        ),
    )
}
