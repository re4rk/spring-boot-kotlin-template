package io.dodn.springboot.core.api.controller.v1

import io.dodn.springboot.core.api.controller.v1.request.CreateOrderRequestDto
import io.dodn.springboot.core.api.controller.v1.response.OrderResponseDto
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
    fun createOrder(@RequestBody request: CreateOrderRequestDto): ApiResponse<OrderResponseDto> {
        val orderData = request.toOrderData()
        val result = orderService.createOrder(orderData)
        return ApiResponse.success(OrderResponseDto.from(result))
    }

    @PostMapping("/external-payment")
    fun createOrderWithExternalPayment(@RequestBody request: CreateOrderRequestDto): ApiResponse<OrderResponseDto> {
        val orderData = request.toOrderData()
        val result = orderService.createOrderWithExternalPayment(orderData)
        return ApiResponse.success(OrderResponseDto.from(result))
    }
}
