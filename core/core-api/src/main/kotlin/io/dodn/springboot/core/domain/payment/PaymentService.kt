package io.dodn.springboot.core.domain.payment

import io.dodn.springboot.client.payment.PaymentClient
import io.dodn.springboot.core.support.error.CoreException
import io.dodn.springboot.core.support.error.ErrorType
import io.dodn.springboot.storage.db.core.payment.PaymentEntity
import io.dodn.springboot.storage.db.core.payment.PaymentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val paymentClient: PaymentClient,
) {
    // 내부 결제 처리
    @Transactional
    fun processPayment(userId: Long, orderId: Long, amount: Long): PaymentResult {
        val payment = PaymentEntity(
            orderId = orderId,
            userId = userId,
            amount = amount,
            status = "PROCESSING",
        )

        val savedPayment = paymentRepository.save(payment)

        // 비즈니스 로직 (결제 유효성 검사 등)
        if (amount <= 0) {
            savedPayment.status = "FAILED"
            paymentRepository.save(savedPayment)
            throw CoreException(ErrorType.INVALID_PAYMENT_AMOUNT)
        }

        savedPayment.status = "SUCCESS"
        paymentRepository.save(savedPayment)

        return PaymentResult(
            paymentId = savedPayment.id,
            status = "SUCCESS",
        )
    }

    // 외부 결제 처리 (분산 트랜잭션)
    @Transactional
    fun processExternalPayment(userId: Long, orderId: Long, amount: Long): PaymentResult {
        // 로컬 Payment 엔티티 생성
        val payment = PaymentEntity(
            orderId = orderId,
            userId = userId,
            amount = amount,
            status = "PROCESSING",
        )

        val savedPayment = paymentRepository.save(payment)

        try {
            // 외부 결제 API 호출 (트랜잭션 경계 넘음)
            val clientResult = paymentClient.processPayment(orderId, userId, amount)

            // 외부 결제 결과 저장
            savedPayment.externalPaymentId = clientResult.paymentId
            savedPayment.status = clientResult.status
            paymentRepository.save(savedPayment)

            if (clientResult.status != "SUCCESS") {
                throw CoreException(ErrorType.EXTERNAL_PAYMENT_FAILED)
            }

            return PaymentResult(
                paymentId = savedPayment.id,
                status = clientResult.status,
            )
        } catch (e: Exception) {
            savedPayment.status = "FAILED"
            paymentRepository.save(savedPayment)
            throw e
        }
    }

    // 외부 결제 취소 (보상 트랜잭션)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun cancelExternalPayment(orderId: Long) {
        val payment = paymentRepository.findAll()
            .firstOrNull { it.orderId == orderId && it.status == "SUCCESS" }
            ?: throw CoreException(ErrorType.PAYMENT_NOT_FOUND)

        val externalPaymentId = payment.externalPaymentId
            ?: throw CoreException(ErrorType.EXTERNAL_PAYMENT_ID_NOT_FOUND)

        // 외부 API로 취소 요청
        val cancelResult = paymentClient.cancelPayment(externalPaymentId)

        // 취소 상태 업데이트
        payment.status = "CANCELLED"
        paymentRepository.save(payment)
    }
}
