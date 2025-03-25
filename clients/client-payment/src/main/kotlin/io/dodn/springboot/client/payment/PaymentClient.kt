package io.dodn.springboot.client.payment

import io.dodn.springboot.client.payment.model.PaymentClientResult
import org.springframework.stereotype.Component

@Component
class PaymentClient internal constructor(
    private val paymentApi: PaymentApi
) {
    fun processPayment(orderId: Long, userId: Long, amount: Long): PaymentClientResult {
        val request = PaymentRequestDto(orderId, userId, amount)
        return paymentApi.processPayment(request).toResult()
    }
    
    fun cancelPayment(paymentId: String): PaymentClientResult {
        return paymentApi.cancelPayment(paymentId).toResult()
    }
}