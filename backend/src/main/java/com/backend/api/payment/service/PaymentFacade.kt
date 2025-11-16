package com.backend.api.payment.service

import com.backend.api.payment.client.TossPaymentClient
import com.backend.api.payment.dto.request.PaymentRequest
import com.backend.api.payment.dto.response.PaymentResponse
import com.backend.domain.payment.entity.Payment
import com.backend.domain.payment.entity.PaymentStatus
import com.backend.global.Rq.Rq
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class PaymentFacade(
    private val tossPaymentClient: TossPaymentClient,
    private val paymentService: PaymentService,
    private val rq: Rq
) {

    fun confirmPayment(request: PaymentRequest): PaymentResponse {
        val user = rq.getUser()

        val response = tossPaymentClient.confirmPayment(request)

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

        val savedPayment = paymentService.savePayment(payment)

        return PaymentResponse.from(savedPayment)
    }
}