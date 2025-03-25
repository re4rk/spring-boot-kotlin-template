package io.dodn.springboot.support.notifiaction.sms

import io.dodn.springboot.client.coolsms.CoolsmsClient
import io.dodn.springboot.client.coolsms.CoolsmsProperties
import io.dodn.springboot.client.coolsms.model.SmsResult
import io.dodn.springboot.support.notification.sms.SmsSender
import io.dodn.springboot.support.notification.sms.model.SmsRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SmsSenderTest {
    private lateinit var coolsmsClient: CoolsmsClient
    private lateinit var coolsmsProperties: CoolsmsProperties
    private lateinit var smsSender: SmsSender

    @BeforeEach
    fun setUp() {
        coolsmsClient = mockk()
        coolsmsProperties = mockk()
        smsSender = SmsSender(coolsmsClient, coolsmsProperties)

        every { coolsmsProperties.defaultSender } returns "01012345678"
    }

    @Test
    fun shouldSendSmsWithDefaultSender() {
        // Given
        val request = SmsRequest(
            to = "01098765432",
            content = "Test SMS message",
        )

        val smsResult = SmsResult(
            messageId = "MSG-ID-123",
            success = true,
            statusCode = "200",
            statusMessage = "Success",
        )

        every { coolsmsClient.sendSms(any(), any(), any()) } returns smsResult

        // When
        val response = smsSender.sendSms(request)

        // Then
        verify { coolsmsClient.sendSms("01098765432", "01012345678", "Test SMS message") }
        assertEquals("MSG-ID-123", response.messageId)
        assertEquals(true, response.success)
    }

    @Test
    fun shouldSendSmsWithCustomSender() {
        // Given
        val request = SmsRequest(
            to = "01098765432",
            from = "01011112222",
            content = "Test SMS message",
        )

        val smsResult = SmsResult(
            messageId = "MSG-ID-123",
            success = true,
            statusCode = "200",
            statusMessage = "Success",
        )

        every { coolsmsClient.sendSms(any(), any(), any()) } returns smsResult

        // When
        val response = smsSender.sendSms(request)

        // Then
        verify { coolsmsClient.sendSms("01098765432", "01011112222", "Test SMS message") }
        assertEquals("MSG-ID-123", response.messageId)
        assertEquals(true, response.success)
    }

    @Test
    fun shouldSendLmsWhenSubjectProvided() {
        // Given
        val request = SmsRequest(
            to = "01098765432",
            content = "Test LMS message with longer content",
            subject = "Test Subject",
        )

        val smsResult = SmsResult(
            messageId = "MSG-ID-456",
            success = true,
            statusCode = "200",
            statusMessage = "Success",
        )

        every { coolsmsClient.sendLms(any(), any(), any(), any()) } returns smsResult

        // When
        val response = smsSender.sendSms(request)

        // Then
        verify {
            coolsmsClient.sendLms(
                "01098765432",
                "01012345678",
                "Test Subject",
                "Test LMS message with longer content",
            )
        }
        assertEquals("MSG-ID-456", response.messageId)
        assertEquals(true, response.success)
    }
}
