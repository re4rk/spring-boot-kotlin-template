package io.dodn.springboot.core.domain.payment

data class PaymentResult(
    val paymentId: Long,
    val status: String
)