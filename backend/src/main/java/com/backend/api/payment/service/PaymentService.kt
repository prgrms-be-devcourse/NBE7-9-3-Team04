package com.backend.api.payment.service

import com.backend.api.payment.dto.request.PaymentRequest
import com.backend.api.payment.dto.response.PaymentConfirmResponse
import com.backend.api.payment.dto.response.PaymentResponse
import com.backend.api.payment.dto.response.PaymentResponse.Companion.from
import com.backend.domain.payment.entity.Payment
import com.backend.domain.payment.entity.PaymentStatus
import com.backend.domain.payment.repository.PaymentRepository
import com.backend.global.Rq.Rq
import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val webClient: WebClient,
    private val rq: Rq
) {

    //결제 승인
    @Transactional
    fun confirmPayment(request: PaymentRequest): PaymentResponse {
        val user = rq.getUser()
        val response = webClient.post()
            .uri("/confirm") // 결제 승인 API 호출
            .bodyValue(request)
            .retrieve()
            .onStatus({ it.isError }) { clientResponse ->
                clientResponse.bodyToMono(String::class.java)
                    .flatMap { errorBody ->
                        Mono.error(ErrorException(errorBody, ErrorCode.PAYMENT_APPROVE_FAILED))
                    }
            }
            .bodyToMono(PaymentConfirmResponse::class.java)
            .block()
            ?: throw ErrorException(ErrorCode.PAYMENT_APPROVE_FAILED)

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

        paymentRepository.save(payment)

        return from(payment)
    }

    @Transactional(readOnly = true)
    fun geyPaymentByKey(paymentKey: String): PaymentResponse {
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
    fun savePayment(payment: Payment) {
        paymentRepository.save(payment)
    }
}
