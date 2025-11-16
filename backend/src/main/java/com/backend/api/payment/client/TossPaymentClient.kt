package com.backend.api.payment.client

import com.backend.api.payment.dto.request.PaymentRequest
import com.backend.api.payment.dto.response.PaymentConfirmResponse
import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class TossPaymentClient(
    private val webClient: WebClient
) {
    fun confirmPayment(request: PaymentRequest): PaymentConfirmResponse {
        return webClient.post()
            .uri("/confirm") // 결제 승인 API 호출
            .bodyValue(request)
            .retrieve()
            .bodyToMono(PaymentConfirmResponse::class.java)
            .block()
            ?: throw ErrorException(ErrorCode.PAYMENT_APPROVE_FAILED)
    }

}