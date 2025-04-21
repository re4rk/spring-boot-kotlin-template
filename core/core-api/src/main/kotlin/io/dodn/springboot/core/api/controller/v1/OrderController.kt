package io.dodn.springboot.core.api.controller.v1

import io.dodn.springboot.core.api.controller.v1.request.CreateOrderRequest
import io.dodn.springboot.core.api.controller.v1.response.OrderResponse
import io.dodn.springboot.core.domain.order.OrderService
import io.dodn.springboot.core.support.response.ApiResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/orders")
class OrderController(
    private val orderService: OrderService,
) {
    @PostMapping
    fun createOrder(@RequestBody request: CreateOrderRequest): ApiResponse<OrderResponse> {
        val orderData = request.toOrderData()
        val result = orderService.createOrder(orderData)
        return ApiResponse.success(OrderResponse.from(result))
    }

    @PostMapping("/external-payment")
    fun createOrderWithExternalPayment(@RequestBody request: CreateOrderRequest): ApiResponse<OrderResponse> {
        val orderData = request.toOrderData()
        val result = orderService.createOrderWithExternalPayment(orderData)
        return ApiResponse.success(OrderResponse.from(result))
    }
}
