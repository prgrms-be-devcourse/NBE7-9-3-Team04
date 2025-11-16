package com.backend.api.payment.service

import com.backend.api.payment.client.TossPaymentClient
import com.backend.api.payment.dto.request.PaymentRequest
import com.backend.api.payment.dto.response.PaymentResponse
import com.backend.global.Rq.Rq
import org.springframework.stereotype.Service

@Service
class PaymentFacade(
    private val tossPaymentClient: TossPaymentClient,
    private val paymentService: PaymentService,
    private val rq: Rq
) {

    suspend fun confirmPayment(request: PaymentRequest): PaymentResponse {
        val user = rq.getUser()

        val response = tossPaymentClient.confirmPayment(request)

        val savedPayment = paymentService.createPayment(response, user)

        return PaymentResponse.from(savedPayment)
    }
}