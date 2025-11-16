package com.backend.api.billing.client

import com.backend.api.billing.dto.response.BillingPaymentResponse
import com.backend.domain.subscription.entity.Subscription
import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.util.*

//외부 API 통신
@Component
class TossBillingClient (
    private val webClient: WebClient
) {

    fun issueBillingKey(authKey: String, customerKey: String): String {
        val response = webClient.post()
            .uri("v1/billing/authorizations/issue")
            .bodyValue(
                mapOf(
                    "authKey" to authKey,
                    "customerKey" to customerKey
                )
            )
            .retrieve()
            .bodyToMono<Map<String, Any>>()
            .block()
            ?: throw ErrorException(ErrorCode.BILLING_RESPONSE_ERROR)

        return response["billingKey"] as? String
            ?: throw ErrorException(ErrorCode.BILLING_KEY_NOT_FOUND)
    }

    fun requestAutoPayment(subscription: Subscription): BillingPaymentResponse {
        val billingKey = subscription.billingKey
            ?: throw ErrorException(ErrorCode.BILLING_KEY_NOT_FOUND)

        return webClient.post()
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
            .block()
            ?: throw ErrorException(ErrorCode.AUTO_PAYMENT_FAILED)
    }
}