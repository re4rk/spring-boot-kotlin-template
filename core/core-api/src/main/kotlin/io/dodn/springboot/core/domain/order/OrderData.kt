package io.dodn.springboot.core.domain.order

data class OrderData(
    val userId: Long,
    val productId: Long,
    val quantity: Int,
    val amount: Long,
)
