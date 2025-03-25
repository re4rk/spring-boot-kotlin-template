package io.dodn.springboot.storage.db.core.order

import io.dodn.springboot.storage.db.CoreDbContextTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.context.jdbc.Sql

class OrderRepositoryIT(
    private val orderRepository: OrderRepository
) : CoreDbContextTest() {

    @Test
    fun `should save and find order`() {
        // Given
        val order = OrderEntity(
            userId = 100L,
            productId = 200L,
            quantity = 2,
            amount = 50000L,
            status = "PENDING"
        )
        
        // When
        val savedOrder = orderRepository.save(order)
        
        // Then
        assertThat(savedOrder.id).isPositive()
        assertThat(savedOrder.userId).isEqualTo(100L)
        assertThat(savedOrder.productId).isEqualTo(200L)
        assertThat(savedOrder.quantity).isEqualTo(2)
        assertThat(savedOrder.amount).isEqualTo(50000L)
        assertThat(savedOrder.status).isEqualTo("PENDING")
        
        // When
        val foundOrder = orderRepository.findById(savedOrder.id).orElseThrow()
        
        // Then
        assertThat(foundOrder.id).isEqualTo(savedOrder.id)
        assertThat(foundOrder.userId).isEqualTo(100L)
        assertThat(foundOrder.productId).isEqualTo(200L)
        assertThat(foundOrder.quantity).isEqualTo(2)
        assertThat(foundOrder.amount).isEqualTo(50000L)
        assertThat(foundOrder.status).isEqualTo("PENDING")
    }
    
    @Test
    fun `should update order status`() {
        // Given
        val order = OrderEntity(
            userId = 100L,
            productId = 200L,
            quantity = 2,
            amount = 50000L,
            status = "PENDING"
        )
        val savedOrder = orderRepository.save(order)
        
        // When
        savedOrder.updateStatus("COMPLETED")
        orderRepository.save(savedOrder)
        
        // Then
        val updatedOrder = orderRepository.findById(savedOrder.id).orElseThrow()
        assertThat(updatedOrder.status).isEqualTo("COMPLETED")
    }
}