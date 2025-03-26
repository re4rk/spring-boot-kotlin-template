package io.dodn.springboot.client.coolsms

import feign.RetryableException
import io.dodn.springboot.client.CoolsmsClientContextTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CoolsmsClientTest(
    val coolsmsClient: CoolsmsClient,
) : CoolsmsClientContextTest() {
    @Test
    fun shouldBeThrownExceptionWhenSendSms() {
        val exception = assertThrows<RetryableException> {
            coolsmsClient.sendSms("01012345678", "01098765432", "Hello, SMS test")
        }

        assertThat(exception).isExactlyInstanceOf(RetryableException::class.java)
    }

    @Test
    fun shouldBeThrownExceptionWhenSendLms() {
        val exception = assertThrows<RetryableException> {
            coolsmsClient.sendLms("01012345678", "01098765432", "Test Subject", "Hello, LMS test with longer content")
        }

        assertThat(exception).isExactlyInstanceOf(RetryableException::class.java)
    }
}
