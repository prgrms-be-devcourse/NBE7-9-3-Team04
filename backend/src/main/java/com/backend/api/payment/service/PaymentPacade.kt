package com.backend.api.payment.service


import com.backend.api.payment.client.TossPaymentClient
import com.backend.api.payment.dto.request.PaymentCancelRequest
import com.backend.api.payment.dto.request.PaymentRequest
import com.backend.api.payment.dto.response.PaymentResponse
import com.backend.global.Rq.Rq
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PaymentFacade(
    private val tossPaymentClient: TossPaymentClient,
    private val paymentService: PaymentService,
    private val rq: Rq
) {
    private val log = LoggerFactory.getLogger(PaymentFacade::class.java)


    fun confirmPayment(request: PaymentRequest): PaymentResponse {
        val user = rq.getUser()
        val response = tossPaymentClient.confirmPayment(request)

        //승인 API 성공 but DB 저장 실패 시 취소 API 실행
        return try{
            val payment = paymentService.createPayment(response, user)
            PaymentResponse.from(payment) }
        catch (e: Exception){
            log.error("Payment DB save failed. Trying to cancel paymentKey=${response.paymentKey}", e)
        //취소 API도 실패 시
        try {
            tossPaymentClient.cancelPayment(
                paymentKey = response.paymentKey,
                request = PaymentCancelRequest(cancelReason = "DB transaction failed")
            )
        } catch (cancelError: Exception) {
            log.error("Cancel failed for paymentKey=${response.paymentKey}", cancelError)
        }
            throw e
        }
    }
}