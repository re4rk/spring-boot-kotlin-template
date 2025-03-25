package io.dodn.springboot.storage.db.core.payment

import io.dodn.springboot.storage.db.CoreDbContextTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql

class PaymentRepositoryIT(
    private val paymentRepository: PaymentRepository
) : CoreDbContextTest() {

    @Test
    fun `should save and find payment`() {
        // Given
        val payment = PaymentEntity(
            orderId = 1L,
            userId = 100L,
            amount = 50000L,
            status = "PROCESSING"
        )
        
        // When
        val savedPayment = paymentRepository.save(payment)
        
        // Then
        assertThat(savedPayment.id).isPositive()
        assertThat(savedPayment.orderId).isEqualTo(1L)
        assertThat(savedPayment.userId).isEqualTo(100L)
        assertThat(savedPayment.amount).isEqualTo(50000L)
        assertThat(savedPayment.status).isEqualTo("PROCESSING")
        assertThat(savedPayment.externalPaymentId).isNull()
        
        // When
        val foundPayment = paymentRepository.findById(savedPayment.id).orElseThrow()
        
        // Then
        assertThat(foundPayment.id).isEqualTo(savedPayment.id)
        assertThat(foundPayment.orderId).isEqualTo(1L)
        assertThat(foundPayment.userId).isEqualTo(100L)
        assertThat(foundPayment.amount).isEqualTo(50000L)
        assertThat(foundPayment.status).isEqualTo("PROCESSING")
    }
    
    @Test
    fun `should update payment status and external ID`() {
        // Given
        val payment = PaymentEntity(
            orderId = 1L,
            userId = 100L,
            amount = 50000L,
            status = "PROCESSING"
        )
        val savedPayment = paymentRepository.save(payment)
        
        // When
        savedPayment.status = "SUCCESS"
        savedPayment.externalPaymentId = "payment-123"
        paymentRepository.save(savedPayment)
        
        // Then
        val updatedPayment = paymentRepository.findById(savedPayment.id).orElseThrow()
        assertThat(updatedPayment.status).isEqualTo("SUCCESS")
        assertThat(updatedPayment.externalPaymentId).isEqualTo("payment-123")
    }
    
    @Test
    // Given
    @Sql("/test-data/payments.sql")
    fun `should find payments by order ID`() {
        // When
        val payments = paymentRepository.findAll()
            .filter { it.orderId == 100L }
        
        // Then
        assertThat(payments).isNotEmpty()
        assertThat(payments[0].orderId).isEqualTo(100L)
    }
}