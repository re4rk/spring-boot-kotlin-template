package io.dodn.springboot.core.api.controller.v1.response

import io.dodn.springboot.core.domain.order.OrderResult

data class OrderResponse(
    val orderId: Long,
    val status: String,
    val paymentId: Long,
) {
    companion object {
        fun from(result: OrderResult): OrderResponse {
            return OrderResponse(
                orderId = result.orderId,
                status = result.status,
                paymentId = result.paymentId,
            )
        }
    }
}
