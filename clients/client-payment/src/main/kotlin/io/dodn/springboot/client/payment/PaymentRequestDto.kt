package io.dodn.springboot.client.payment

import io.dodn.springboot.client.payment.model.PaymentClientResult

internal data class PaymentRequestDto(
    val orderId: Long,
    val userId: Long,
    val amount: Long
)

internal data class PaymentResponseDto(
    val paymentId: String,
    val status: String,
    val message: String?
) {
    fun toResult(): PaymentClientResult {
        return PaymentClientResult(
            paymentId = paymentId,
            status = status
        )
    }
}