package io.dodn.springboot.storage.db.core.order

import io.dodn.springboot.storage.db.core.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "orders")
class OrderEntity(
    @Column(nullable = false)
    val userId: Long,
    
    @Column(nullable = false)
    val productId: Long,
    
    @Column(nullable = false)
    val quantity: Int,
    
    @Column(nullable = false)
    val amount: Long,
    
    @Column(nullable = false)
    var status: String
) : BaseEntity() {

    fun updateStatus(newStatus: String) {
        this.status = newStatus
    }
}