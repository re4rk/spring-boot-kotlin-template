package io.dodn.springboot.core.api.controller.v1.request

import io.dodn.springboot.core.domain.order.OrderData

data class CreateOrderRequestDto(
    val userId: Long,
    val productId: Long,
    val quantity: Int,
    val amount: Long,
) {
    fun toOrderData(): OrderData {
        return OrderData(
            userId = userId,
            productId = productId,
            quantity = quantity,
            amount = amount,
        )
    }
}
