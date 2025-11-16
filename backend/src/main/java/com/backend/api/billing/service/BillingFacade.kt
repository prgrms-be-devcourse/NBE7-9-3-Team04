package com.backend.api.billing.service

import com.backend.api.billing.client.TossBillingClient
import com.backend.api.billing.dto.request.BillingRequest
import com.backend.api.billing.dto.response.BillingResponse
import com.backend.api.payment.service.PaymentService
import com.backend.api.subscription.service.SubscriptionService
import com.backend.domain.payment.entity.Payment
import com.backend.domain.payment.entity.PaymentStatus
import com.backend.domain.subscription.entity.Subscription
import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

//DB 작어
@Service
class BillingFacade(
    private val tossBillingClient: TossBillingClient,
    private val subscriptionService: SubscriptionService,
    private val paymentService: PaymentService
) {

    fun issueBillingKey(request: BillingRequest): BillingResponse {

        val authKey = request.authKey ?: throw ErrorException(ErrorCode.INVALID_AUTH_KEY)
        val customerKey = request.customerKey ?: throw ErrorException(ErrorCode.INVALID_CUSTOMER_KEY)

        val billingKey = tossBillingClient.issueBillingKey(authKey, customerKey)

        subscriptionService.activatePremium(customerKey, billingKey)

        val sub = subscriptionService.getSubscriptionByCustomerKey(customerKey)
        autoPayment(sub)

        return BillingResponse(billingKey, customerKey)
    }

    fun autoPayment(subscription: Subscription): Payment {

        val response = tossBillingClient.requestAutoPayment(subscription)

        val approvedAt = LocalDateTime.parse(
            response.approvedAt,
            DateTimeFormatter.ISO_OFFSET_DATE_TIME
        )

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

        val savedPayment = paymentService.savePayment(payment)
        subscriptionService.updateNextBillingDate(subscription, LocalDate.now().plusMonths(1))

        return savedPayment
    }

}