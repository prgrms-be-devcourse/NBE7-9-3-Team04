package com.backend.api.subscription.service

import com.backend.api.subscription.dto.response.SubscriptionResponse
import com.backend.api.subscription.dto.response.SubscriptionResponse.Companion.from
import com.backend.api.user.service.UserService
import com.backend.domain.subscription.entity.Subscription
import com.backend.domain.subscription.repository.SubscriptionRepository
import com.backend.domain.user.entity.User
import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class SubscriptionService(
    private val subscriptionRepository: SubscriptionRepository,
    private val userService: UserService
) {

    @Transactional
    fun activatePremium(customerKey: String, billingKey: String): SubscriptionResponse {

        val subscription = subscriptionRepository.findByCustomerKey(customerKey)
            ?: throw ErrorException(ErrorCode.SUBSCRIPTION_NOT_FOUND)

        if (subscription.active) {
            throw ErrorException(ErrorCode.SUBSCRIPTION_ALREADY_EXISTS)
        }

        if (billingKey.isBlank()) {
            throw ErrorException(ErrorCode.BILLING_KEY_NOT_FOUND)
        }

        subscription.activatePremium(billingKey)
        subscriptionRepository.save(subscription)
        return from(subscription)
    }

    @Transactional
    fun getSubscriptionByUserId(userId: Long): SubscriptionResponse {
        val user = userService.getUser(userId)

        val subscription: Subscription = subscriptionRepository.findByUser(user)
            ?: throw ErrorException(ErrorCode.SUBSCRIPTION_NOT_FOUND)

        return from(subscription)
    }

    @Transactional(readOnly = true)
    fun getSubscriptionByCustomerKey(customerKey: String): Subscription {
        return subscriptionRepository.findByCustomerKey(customerKey)
            ?: throw ErrorException(ErrorCode.SUBSCRIPTION_NOT_FOUND)
    }

    @Transactional(readOnly = true)
    fun getSubscriptionById(id: Long): Subscription {
        return subscriptionRepository.findById(id).orElseThrow {
            ErrorException(ErrorCode.SUBSCRIPTION_NOT_FOUND)
        }
    }

    @Transactional
    fun updateNextBillingDate(subscription: Subscription, nextDate: LocalDate) {
        subscription.updateNextBillingDate(nextDate)
        subscriptionRepository.save(subscription)
    }

    @Transactional
    fun cancelSubscription(customerKey: String): SubscriptionResponse {
        val subscription: Subscription = subscriptionRepository.findByCustomerKey(customerKey)
            ?: throw ErrorException(ErrorCode.SUBSCRIPTION_NOT_FOUND)

        if (!subscription.active) { //이미 비활성화된 구독은 처리 x
            throw ErrorException(ErrorCode.SUBSCRIPTION_INACTIVE)
        }

        subscription.billingKey = null

        //다음 결제일까지는 구독 유효하도록
        subscriptionRepository.save(subscription)

        return from(subscription)
    }

    @Transactional
    fun saveSubscription(subscription: Subscription) {
        subscriptionRepository.save(subscription)
    }

    @Transactional(readOnly = true)
    fun getActiveSubscriptionsByBillingDate(billingDate: LocalDate): List<Subscription> {
        return subscriptionRepository.findByNextBillingDateAndActive(billingDate, true)
    }

    fun isPremium(user: User): Boolean {
        val subscription = subscriptionRepository.findByUser(user)
        return subscription?.isValid() ?: false
    }

    fun getAiQuestionLimit(user: User): Int{
        val subscription = subscriptionRepository.findByUser(user)
        return subscription?.questionLimit ?: 5
    }
}
