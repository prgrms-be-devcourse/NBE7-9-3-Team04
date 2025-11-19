package com.backend.api.billing.service

import com.backend.api.billing.client.TossBillingClient
import com.backend.api.billing.dto.request.BillingRequest
import com.backend.api.billing.dto.response.BillingResponse
import com.backend.domain.subscription.entity.Subscription
import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import org.springframework.stereotype.Service

@Service
class BillingFacade(
    private val tossBillingClient: TossBillingClient,
    private val billingService: BillingService,
) {

    private val log = org.slf4j.LoggerFactory.getLogger(BillingFacade::class.java)


    fun issueBillingKey(request: BillingRequest): BillingResponse {
        val authKey = request.authKey ?: throw ErrorException(ErrorCode.INVALID_AUTH_KEY)
        val customerKey = request.customerKey ?: throw ErrorException(ErrorCode.INVALID_CUSTOMER_KEY)

        val billingKey = tossBillingClient.issueBillingKey(authKey, customerKey)

        val subscription = billingService.activatePremium(customerKey, billingKey)

        val body = billingService.buildPaymentBody(subscription)

        val response = tossBillingClient.billingPayment(billingKey, body)

        billingService.processAutoPayment(customerKey, response)

        return BillingResponse(
            billingKey = billingKey,
            customerKey = subscription.customerKey
        )
    }



    fun autoPayment(subscription: Subscription) {
        if (!subscription.active) {
            throw ErrorException(ErrorCode.SUBSCRIPTION_INACTIVE)
        }

        val billingKey = subscription.billingKey
            ?: throw ErrorException(ErrorCode.BILLING_KEY_NOT_FOUND)

        val body = billingService.buildPaymentBody(subscription)
        val response = tossBillingClient.billingPayment(billingKey, body)

        billingService.processAutoPayment(subscription.customerKey, response)
    }
}