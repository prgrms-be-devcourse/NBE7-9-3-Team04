package com.backend.api.billing.service

import com.backend.api.billing.dto.request.BillingRequest
import com.backend.api.billing.dto.response.BillingPaymentResponse
import com.backend.api.billing.dto.response.BillingResponse
import com.backend.api.payment.dto.response.PaymentResponse.Companion.from
import com.backend.api.payment.service.PaymentService
import com.backend.api.subscription.service.SubscriptionService
import com.backend.domain.payment.entity.Payment
import com.backend.domain.payment.entity.PaymentStatus
import com.backend.domain.subscription.entity.Subscription
import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


@Service
class BillingService(
    private val subscriptionService: SubscriptionService,
    private val paymentService: PaymentService,
    private val webClient: WebClient
) {
    //빌링키 발급
    @Transactional
    fun issueBillingKey(request: BillingRequest): BillingResponse {

        val authKey = request.authKey
            ?: throw ErrorException(ErrorCode.INVALID_AUTH_KEY)

        val customerKey = request.customerKey
            ?: throw ErrorException(ErrorCode.INVALID_CUSTOMER_KEY)

        val billingResponse = webClient.post()
            .uri("v1/billing/authorizations/issue")
            .bodyValue(
                mapOf(
                    "authKey" to authKey,
                    "customerKey" to customerKey
                )
            )
            .retrieve()
            .bodyToMono<Map<String, Any>>()
            .block() ?: throw ErrorException(ErrorCode.BILLING_RESPONSE_ERROR)

        val billingKey = billingResponse["billingKey"] as? String
            ?: throw ErrorException(ErrorCode.BILLING_KEY_NOT_FOUND)

        // 구독 PREMIUM 전환
        val updated = subscriptionService.activatePremium(customerKey, billingKey)

        //카드 등록 시 첫번째 결제 바로 실행
        val subscription = subscriptionService.getSubscriptionByCustomerKey(customerKey)
        autoPayment(subscription)

        return BillingResponse(billingKey, updated.customerKey!!)
    }

    //자동 결제. 한달 주기로 결제 진행
    @Transactional
    fun autoPayment(subscription: Subscription) {
        val managedSub = subscriptionService.getSubscriptionById(subscription.id)

        val billingKey = subscription.billingKey
            ?: throw ErrorException(ErrorCode.BILLING_KEY_NOT_FOUND)

        if (!subscription.active) {
            throw ErrorException(ErrorCode.SUBSCRIPTION_INACTIVE)
        }

        val response = webClient.post()
            .uri("/v1/billing/$billingKey")
            .bodyValue(
                mapOf(
                    "customerKey" to subscription.customerKey,
                    "amount" to subscription.price,
                    "orderId" to UUID.randomUUID().toString(),
                    "orderName" to "프리미엄 구독 자동결제",
                    "customerEmail" to subscription.user.email,
                    "customerName" to subscription.user.name
                )
            )
            .retrieve()
            .bodyToMono<BillingPaymentResponse>()
            .block() ?: throw ErrorException(ErrorCode.AUTO_PAYMENT_FAILED)

        val approvedAt = LocalDateTime.parse(response.approvedAt, DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        val payment = Payment(
            orderId = response.orderId,
            paymentKey = response.paymentKey,
            orderName = response.orderName,
            totalAmount = subscription.price,
            method = "CARD",
            status = PaymentStatus.valueOf(response.status),
            approvedAt = approvedAt,
            user = subscription.user,
            subscription = subscription
        )

        paymentService.savePayment(payment)

        subscriptionService.updateNextBillingDate(managedSub, LocalDate.now().plusMonths(1))
        from(payment)
    }
}
