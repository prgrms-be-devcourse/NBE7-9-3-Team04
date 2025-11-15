package com.backend.domain.subscription.entity

import com.backend.domain.user.entity.User
import com.backend.global.entity.BaseEntity
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Entity
class Subscription(

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var subscriptionType: SubscriptionType,

    @Column(nullable = false)
    var active: Boolean = false,

    @Column(nullable = false)
    var startDate: LocalDateTime,

    @Column(nullable = true)
    var endDate: LocalDateTime? = null,

    @Column(nullable = true)
    var nextBillingDate: LocalDate? = null,

    //질문 가능 횟수
    //무료 회원 5번
    //유료 회원 8번
    @Column(nullable = false)
    var questionLimit: Int = 0,

    @Column(nullable = false)
    var subscriptionName: String,

    @Column(nullable = false)
    var price: Long,


    @Column(nullable = true, unique = true)
    var billingKey: String? = null,

    @Column(nullable = false, unique = true)
    var customerKey: String = "",

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    val user: User,

    ) : BaseEntity() {


    fun activatePremium(billingKey: String) {
        this.billingKey = billingKey
        this.active = true
        this.subscriptionType = SubscriptionType.PREMIUM
        this.subscriptionName = "PREMIUM"
        this.price = 9900L
        this.questionLimit = 8
        this.startDate = LocalDateTime.now()
        this.endDate = this.startDate.plusMonths(1)
        this.nextBillingDate = LocalDate.now().plusMonths(1)
    }

    fun deActivatePremium() {
        this.billingKey = null
        this.active = false
        this.subscriptionType = SubscriptionType.BASIC
        this.subscriptionName = "BASIC"
        this.price = 0L
        this.questionLimit = 5
        this.endDate = LocalDateTime.now()
        this.nextBillingDate = null
    }


    @PrePersist
    fun generateCustomerKey() {
        if (this.customerKey.isBlank()) {
            this.customerKey = UUID.randomUUID().toString()
        }
    }

    fun updateNextBillingDate(nextDate: LocalDate) {
        this.nextBillingDate = nextDate
    }

    fun isValid(): Boolean =
        subscriptionType == SubscriptionType.PREMIUM &&
                active &&
                endDate?.isAfter(LocalDateTime.now()) == true

    //TODO 임시 빌더 제거
    class Builder {
        private var subscriptionType: SubscriptionType = SubscriptionType.BASIC
        private var isActive: Boolean = false
        private var startDate: LocalDateTime = LocalDateTime.now()
        private var endDate: LocalDateTime? = null
        private var nextBillingDate: LocalDate? = null
        private var questionLimit: Int = 5
        private var subscriptionName: String = "BASIC"
        private var price: Long = 0L
        private var billingKey: String? = null
        private lateinit var user: User

        fun subscriptionType(subscriptionType: SubscriptionType) = apply { this.subscriptionType = subscriptionType }
        fun isActive(isActive: Boolean) = apply { this.isActive = isActive }
        fun startDate(startDate: LocalDateTime) = apply { this.startDate = startDate }
        fun endDate(endDate: LocalDateTime?) = apply { this.endDate = endDate }
        fun nextBillingDate(nextBillingDate: LocalDate?) = apply { this.nextBillingDate = nextBillingDate }
        fun questionLimit(questionLimit: Int) = apply { this.questionLimit = questionLimit }
        fun subscriptionName(subscriptionName: String) = apply { this.subscriptionName = subscriptionName }
        fun price(price: Long) = apply { this.price = price }
        fun billingKey(billingKey: String?) = apply { this.billingKey = billingKey }
        fun user(user: User) = apply { this.user = user }

        fun build(): Subscription =
            Subscription(
                subscriptionType,
                isActive,
                startDate,
                endDate,
                nextBillingDate,
                questionLimit,
                subscriptionName,
                price,
                billingKey,
                "",
                user
            )
    }

    companion object {
        @JvmStatic
        fun builder(): Builder = Builder()
    }

}
