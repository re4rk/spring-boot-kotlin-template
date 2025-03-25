package io.dodn.springboot.core.domain.order

import io.dodn.springboot.core.domain.payment.PaymentService
import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.core.support.error.ErrorType
import io.dodn.springboot.storage.db.core.order.OrderEntity
import io.dodn.springboot.storage.db.core.order.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val paymentService: PaymentService
) {
    @Transactional
    fun createOrder(orderData: OrderData): OrderResult {
        // 1. 주문 저장
        val order = OrderEntity(
            userId = orderData.userId,
            productId = orderData.productId,
            quantity = orderData.quantity,
            amount = orderData.amount,
            status = "PENDING"
        )
        val savedOrder = orderRepository.save(order)

        try {
            // 2. 결제 처리 (같은 트랜잭션 내에서)
            val paymentResult = paymentService.processPayment(
                userId = orderData.userId,
                orderId = savedOrder.id,
                amount = orderData.amount
            )

            // 3. 주문 상태 업데이트
            if (paymentResult.status == "SUCCESS") {
                savedOrder.updateStatus("COMPLETED")
                orderRepository.save(savedOrder)
            } else {
                throw CoreException(ErrorType.PAYMENT_FAILED)
            }

            return OrderResult(
                orderId = savedOrder.id,
                status = savedOrder.status,
                paymentId = paymentResult.paymentId
            )
        } catch (e: Exception) {
            savedOrder.updateStatus("FAILED")
            orderRepository.save(savedOrder)
            throw e
        }
    }

    @Transactional
    fun createOrderWithExternalPayment(orderData: OrderData): OrderResult {
        // 1. 주문 저장
        val order = OrderEntity(
            userId = orderData.userId,
            productId = orderData.productId,
            quantity = orderData.quantity,
            amount = orderData.amount,
            status = "PENDING"
        )
        val savedOrder = orderRepository.save(order)

        try {
            // 2. 외부 결제 처리
            val paymentResult = paymentService.processExternalPayment(
                userId = orderData.userId,
                orderId = savedOrder.id,
                amount = orderData.amount
            )

            // 3. 주문 상태 업데이트
            if (paymentResult.status == "SUCCESS") {
                savedOrder.updateStatus("COMPLETED")
                orderRepository.save(savedOrder)
            } else {
                savedOrder.updateStatus("FAILED")
                orderRepository.save(savedOrder)
                throw CoreException(ErrorType.PAYMENT_FAILED)
            }

            return OrderResult(
                orderId = savedOrder.id,
                status = savedOrder.status,
                paymentId = paymentResult.paymentId
            )
        } catch (e: Exception) {
            // 외부 결제가 성공했으나 이후 처리가 실패한 경우
            try {
                if (e !is CoreException || e.errorType != ErrorType.PAYMENT_FAILED) {
                    paymentService.cancelExternalPayment(savedOrder.id)
                }
            } catch (cancelEx: Exception) {
                // 취소 실패 로깅 - 별도 처리 필요
            }

            savedOrder.updateStatus("FAILED")
            orderRepository.save(savedOrder)
            throw e
        }
    }
}