package com.backend.api.payment.service

import com.backend.api.payment.dto.response.PaymentConfirmResponse
import com.backend.api.payment.dto.response.PaymentResponse
import com.backend.api.payment.dto.response.PaymentResponse.Companion.from
import com.backend.domain.payment.entity.Payment
import com.backend.domain.payment.entity.PaymentStatus
import com.backend.domain.payment.repository.PaymentRepository
import com.backend.domain.user.entity.User
import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
) {

    @Transactional
    suspend fun createPayment(response: PaymentConfirmResponse, user: User): Payment {

        val payment = Payment(
            orderId = response.orderId,
            paymentKey = response.paymentKey,
            orderName = response.orderName,
            totalAmount = response.totalAmount,
            method = response.method,
            status = PaymentStatus.DONE,
            approvedAt = LocalDateTime.now(),
            user = user
        )

        return paymentRepository.save(payment)
    }

    @Transactional(readOnly = true)
    fun getPaymentByKey(paymentKey: String): PaymentResponse {
        val payment= paymentRepository.findByPaymentKey(paymentKey)
            ?: throw ErrorException(ErrorCode.PAYMENT_NOT_FOUND)

        return from(payment)
    }

    @Transactional(readOnly = true)
    fun getPaymentByOrderId(orderId: String): PaymentResponse {
        val payment = paymentRepository.findByOrderId(orderId)
            ?: throw ErrorException(ErrorCode.PAYMENT_NOT_FOUND)

        return from(payment)
    }

    @Transactional
    fun savePayment(payment: Payment):Payment {
        return paymentRepository.save(payment)
    }
}
