package io.dodn.springboot.core.domain.order

data class OrderResult(
    val orderId: Long,
    val status: String,
    val paymentId: Long,
)
