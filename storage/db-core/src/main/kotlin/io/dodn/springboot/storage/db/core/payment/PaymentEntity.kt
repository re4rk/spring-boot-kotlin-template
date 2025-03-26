package io.dodn.springboot.storage.db.core.payment

import io.dodn.springboot.storage.db.core.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "payments")
class PaymentEntity(
    @Column(nullable = false)
    val orderId: Long,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false)
    val amount: Long,

    @Column(nullable = false)
    var status: String,

    @Column
    var externalPaymentId: String? = null,
) : BaseEntity()
