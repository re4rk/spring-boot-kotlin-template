package io.dodn.springboot.client.payment

import feign.FeignException
import io.dodn.springboot.client.ClientPaymentContextTest
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PaymentClientTest : ClientPaymentContextTest() {
    private lateinit var paymentApi: PaymentApi
    private lateinit var paymentClient: PaymentClient
    
    @BeforeEach
    fun setUp() {
        paymentApi = mockk()
        paymentClient = PaymentClient(paymentApi)
    }
    
    @Test
    fun `should process payment successfully`() {
        // Given
        val orderId = 1L
        val userId = 100L
        val amount = 50000L
        
        val requestSlot = slot<PaymentRequestDto>()
        val responseDto = PaymentResponseDto(
            paymentId = "payment-123",
            status = "SUCCESS",
            message = null
        )
        
        every { paymentApi.processPayment(capture(requestSlot)) } returns responseDto
        
        // When
        val result = paymentClient.processPayment(orderId, userId, amount)
        
        // Then
        verify(exactly = 1) { paymentApi.processPayment(any()) }
        
        val capturedRequest = requestSlot.captured
        assertThat(capturedRequest.orderId).isEqualTo(orderId)
        assertThat(capturedRequest.userId).isEqualTo(userId)
        assertThat(capturedRequest.amount).isEqualTo(amount)
        
        assertThat(result.paymentId).isEqualTo("payment-123")
        assertThat(result.status).isEqualTo("SUCCESS")
    }
    
    @Test
    fun `should handle payment failure`() {
        // Given
        val orderId = 1L
        val userId = 100L
        val amount = 50000L
        
        val responseDto = PaymentResponseDto(
            paymentId = "payment-123",
            status = "FAILED",
            message = "Insufficient funds"
        )
        
        every { paymentApi.processPayment(any()) } returns responseDto
        
        // When
        val result = paymentClient.processPayment(orderId, userId, amount)
        
        // Then
        verify(exactly = 1) { paymentApi.processPayment(any()) }
        
        assertThat(result.paymentId).isEqualTo("payment-123")
        assertThat(result.status).isEqualTo("FAILED")
    }
    
    @Test
    fun `should handle network error`() {
        // Given
        val orderId = 1L
        val userId = 100L
        val amount = 50000L
        
        every { paymentApi.processPayment(any()) } throws FeignException.ServiceUnavailable(
            "Service Unavailable", 
            mockk(relaxed = true), 
            null, 
            emptyMap()
        )
        
        // When & Then
        assertThatThrownBy { 
            paymentClient.processPayment(orderId, userId, amount) 
        }.isInstanceOf(FeignException.ServiceUnavailable::class.java)
    }
    
    @Test
    fun `should cancel payment successfully`() {
        // Given
        val paymentId = "payment-123"
        
        val responseDto = PaymentResponseDto(
            paymentId = paymentId,
            status = "CANCELLED",
            message = null
        )
        
        every { paymentApi.cancelPayment(paymentId) } returns responseDto
        
        // When
        val result = paymentClient.cancelPayment(paymentId)
        
        // Then
        verify(exactly = 1) { paymentApi.cancelPayment(paymentId) }
        
        assertThat(result.paymentId).isEqualTo(paymentId)
        assertThat(result.status).isEqualTo("CANCELLED")
    }
}