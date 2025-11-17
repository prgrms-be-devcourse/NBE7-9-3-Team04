package com.backend.api.payment.service

import com.backend.api.payment.client.TossPaymentClient
import com.backend.api.payment.dto.request.PaymentCancelRequest
import com.backend.api.payment.dto.request.PaymentRequest
import com.backend.api.payment.dto.response.PaymentResponse
import com.backend.global.Rq.Rq
import com.backend.global.lock.DistributedLockManager
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PaymentFacade(
    private val tossPaymentClient: TossPaymentClient,
    private val paymentService: PaymentService,
    private val rq: Rq,
    private val lockManager: DistributedLockManager
) {
    private val log = LoggerFactory.getLogger(PaymentFacade::class.java)


    //orderId 기준 분산락 적용
    suspend fun confirmPayment(request: PaymentRequest): PaymentResponse {
        val lockKey = "lock:payment:{${request.orderId}}"

        return lockManager.withLock(lockKey){
            processConfirmPayment(request)
        }
    }

    //실제 결제 로직(락 내부에서 실행)
    suspend fun processConfirmPayment(request: PaymentRequest): PaymentResponse {
        val user = rq.getUser()

        val existingPayment = paymentService.findByOrderIdOrNull(request.orderId)
        if(existingPayment != null) {
            log.info("이미 승인된 결제 orderId = ${request.orderId}")
            return PaymentResponse.from(existingPayment)
        }

        val response = tossPaymentClient.confirmPayment(request)

        //승인 API 실패 but DB 저장 실패 시 취소 API 실행
        return runCatching {
            val payment = paymentService.createPayment(response, user)
            PaymentResponse.from(payment)
        }.onFailure { e ->
            log.error("DB 저장 실패. Toss 결제 취소 실행 → paymentKey=${response.paymentKey}", e)

            runCatching {
                tossPaymentClient.cancelPayment(
                    paymentKey = response.paymentKey,
                    request = PaymentCancelRequest("DB transaction failed")
                )
            }.onFailure { cancelError ->
                log.error("Toss 결제 취소 실패 → paymentKey=${response.paymentKey}", cancelError)
            }

        }.getOrThrow()
    }
}