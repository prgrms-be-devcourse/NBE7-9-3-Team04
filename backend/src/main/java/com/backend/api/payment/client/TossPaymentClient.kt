package com.backend.api.payment.client

import com.backend.api.payment.dto.request.PaymentCancelRequest
import com.backend.api.payment.dto.request.PaymentRequest
import com.backend.api.payment.dto.response.PaymentCancelResponse
import com.backend.api.payment.dto.response.PaymentConfirmResponse
import com.backend.api.payment.dto.response.TossErrorResponse
import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.*

@Component
class TossPaymentClient(
    private val webClient: WebClient
) {
    suspend fun confirmPayment(request: PaymentRequest): PaymentConfirmResponse {

        return webClient.post()
            .uri("/confirm") // 결제 승인 API 호출
            .bodyValue(request)
            .retrieve()
            // HTTP 상태 코드가 4xx/5xx이면 onStatus에서 처리
            .onStatus({ it.is4xxClientError }) { response ->
                response.bodyToMono(TossErrorResponse::class.java)
                    .flatMap { Mono.error(ErrorException(ErrorCode.PAYMENT_APPROVE_FAILED)) }
            }
            .onStatus({ it.is5xxServerError }) { response ->
                response.bodyToMono(TossErrorResponse::class.java)
                    .flatMap { Mono.error(ErrorException(ErrorCode.PAYMENT_FAILED)) }
            }
            .bodyToMono(PaymentConfirmResponse::class.java)
            .timeout(Duration.ofSeconds(3))
            .awaitSingle() // <-- 코루틴 suspend 로 변환 (block 제거)
    }

    suspend fun cancelPayment(paymentKey: String, request: PaymentCancelRequest): PaymentCancelResponse {

        return webClient.post()
            .uri("/payments/$paymentKey/cancel")
            .header("Idempotency-Key", UUID.randomUUID().toString()) //멱등성
            .bodyValue(request)
            .retrieve()
            // HTTP 상태 코드가 4xx/5xx이면 onStatus에서 처리
            .onStatus({ it.is4xxClientError }) { response ->
                response.bodyToMono(TossErrorResponse::class.java)
                    .flatMap { Mono.error(ErrorException(ErrorCode.PAYMENT_CANCEL_FAILED)) }
            }
            .onStatus({ it.is5xxServerError }) { response ->
                response.bodyToMono(TossErrorResponse::class.java)
                    .flatMap { Mono.error(ErrorException(ErrorCode.PAYMENT_FAILED)) }
            }
            .bodyToMono(PaymentCancelResponse::class.java)
            .awaitSingle() // <-- 코루틴 suspend 로 변환 (block 제거)
    }

}