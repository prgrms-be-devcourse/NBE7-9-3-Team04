package com.backend.api.payment.service

import com.backend.api.payment.dto.response.PaymentResponse
import com.backend.api.payment.dto.response.PaymentResponse.Companion.from
import com.backend.domain.payment.entity.Payment
import com.backend.domain.payment.repository.PaymentRepository
import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
) {

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
