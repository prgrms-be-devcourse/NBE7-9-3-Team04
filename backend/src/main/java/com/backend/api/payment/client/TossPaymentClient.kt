package com.backend.api.payment.client

import com.backend.api.payment.dto.request.PaymentRequest
import com.backend.api.payment.dto.response.PaymentConfirmResponse
import com.backend.api.payment.dto.response.TossErrorResponse
import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.time.Duration

@Component
class TossPaymentClient(
    private val webClient: WebClient
) {
    fun confirmPayment(request: PaymentRequest): Mono<PaymentConfirmResponse> {
        return webClient.post()
            .uri("/confirm") // 결제 승인 API 호출
            .bodyValue(request)
            .retrieve()
            // HTTP 상태 코드가 4xx/5xx이면 onStatus에서 처리
            .onStatus({ status -> status.is4xxClientError }) { response ->
                response.bodyToMono(TossErrorResponse::class.java)
                    .flatMap { Mono.error(ErrorException(ErrorCode.PAYMENT_APPROVE_FAILED)) }
            }
            .onStatus({ status -> status.is5xxServerError }) { response ->
                response.bodyToMono(TossErrorResponse::class.java)
                    .flatMap { Mono.error(ErrorException(ErrorCode.PAYMENT_FAILED)) }
            }
            .bodyToMono(PaymentConfirmResponse::class.java)
            .timeout(Duration.ofSeconds(3))
    }

}