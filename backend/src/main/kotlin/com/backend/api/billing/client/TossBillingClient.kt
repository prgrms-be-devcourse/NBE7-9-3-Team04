package com.backend.api.billing.client

import com.backend.api.billing.dto.response.BillingPaymentResponse
import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class TossBillingClient(
    private val webClient: WebClient
) {

    fun issueBillingKey(authKey: String, customerKey: String): String {
        val billingResponse = webClient.post()
            .uri("/v1/billing/authorizations/issue")
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

        return billingResponse["billingKey"] as? String
            ?: throw ErrorException(ErrorCode.BILLING_KEY_NOT_FOUND)
    }

    fun billingPayment(billingKey: String, body: Map<String, Any>): BillingPaymentResponse {
        return webClient.post()
            .uri("/v1/billing/$billingKey")
            .bodyValue(body)
            .retrieve()
            .bodyToMono(BillingPaymentResponse::class.java)
            .block()
            ?: throw ErrorException(ErrorCode.AUTO_PAYMENT_FAILED)
    }
}