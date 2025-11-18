package com.backend.api.billing.service

import com.backend.api.billing.dto.response.BillingPaymentResponse
import com.backend.api.payment.service.PaymentService
import com.backend.api.subscription.service.SubscriptionService
import com.backend.domain.payment.entity.Payment
import com.backend.domain.payment.entity.PaymentStatus
import com.backend.domain.subscription.entity.Subscription
import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


@Service
class BillingService(
    private val subscriptionService: SubscriptionService,
    private val paymentService: PaymentService,
) {

    @Transactional
    fun activatePremium(customerKey: String, billingKey: String): Subscription {
        subscriptionService.activatePremium(customerKey, billingKey)
        return subscriptionService.getSubscriptionByCustomerKey(customerKey)
    }

    @Transactional
    fun savePayment(subscription: Subscription, paymentRes: BillingPaymentResponse) {
        val approvedAt = LocalDateTime.parse(
            paymentRes.approvedAt,
            DateTimeFormatter.ISO_OFFSET_DATE_TIME
        )

        val payment = Payment(
            orderId = paymentRes.orderId,
            paymentKey = paymentRes.paymentKey,
            orderName = paymentRes.orderName,
            totalAmount = subscription.price,
            method = "CARD",
            status = PaymentStatus.valueOf(paymentRes.status),
            approvedAt = approvedAt,
            user = subscription.user,
            subscription = subscription
        )

        paymentService.savePayment(payment)
    }

    @Transactional
    fun updateNextBillingDate(subscription: Subscription) {
        val next = LocalDate.now().plusMonths(1)
        subscriptionService.updateNextBillingDate(subscription, next)
    }

    @Transactional
    fun processAutoPayment(
        customerKey: String, billingRes: BillingPaymentResponse
    ) {
        // 비관적 락 획득
        val lockedSubscription = subscriptionService.getSubscriptionForUpdate(customerKey)

        if (!lockedSubscription.active)
            throw ErrorException(ErrorCode.SUBSCRIPTION_INACTIVE)


        // 결제 저장
        savePayment(lockedSubscription, billingRes)

        updateNextBillingDate(lockedSubscription)
    }



    //외부 API 요청 body 생성
    fun buildPaymentBody(subscription: Subscription): Map<String, Any> {
        return mapOf(
            "customerKey" to subscription.customerKey,
            "amount" to subscription.price,
            "orderId" to UUID.randomUUID().toString(),
            "orderName" to "프리미엄 구독 자동결제",
            "customerEmail" to subscription.user.email,
            "customerName" to subscription.user.name
        )
    }
}
