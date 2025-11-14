package com.backend.domain.payment.entity

import com.backend.domain.subscription.entity.Subscription
import com.backend.domain.user.entity.User
import com.backend.global.entity.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class Payment(
    //주문 번호.
    @Column(nullable = false, unique = true)
    var orderId: String,

    //Toss가 제공하는 결제의 키 값
    //결제 승인 API 응답에서만 받음
    @Column(unique = true, length = 200)
    var paymentKey: String? = null,

    //주문 내역
    @Column(nullable = false)
    var orderName: String,

    //총 결제 금액
    @Column(nullable = false)
    var totalAmount: Long,

    //결제 방식
    var method: String? = null,

    //결제 상태(토스 status)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PaymentStatus,

    //결제 승인 시간
    @Column(name = "approved_at")
    var approvedAt: LocalDateTime? = null,

    //subscription과 연결되기 때문에 빼도 되지만 일단 남겨둠
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @ManyToOne(fetch = FetchType.LAZY) //한 구독에 결제는 여러번
    @JoinColumn(name = "subscription_id", nullable = false)
    var subscription: Subscription ?= null

) : BaseEntity() {

    //TODO 임시 빌더 제거
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var orderId: String = ""
        private var paymentKey: String? = null
        private var orderName: String = ""
        private var totalAmount: Long = 0
        private var method: String? = null
        private var status: PaymentStatus = PaymentStatus.REQUESTED
        private var approvedAt: LocalDateTime? = null
        private var user: User? = null
        private var subscription: Subscription? = null

        fun orderId(orderId: String) = apply { this.orderId = orderId }
        fun paymentKey(paymentKey: String?) = apply { this.paymentKey = paymentKey }
        fun orderName(orderName: String) = apply { this.orderName = orderName }
        fun totalAmount(totalAmount: Long) = apply { this.totalAmount = totalAmount }
        fun method(method: String?) = apply { this.method = method }
        fun status(status: PaymentStatus) = apply { this.status = status }
        fun approvedAt(approvedAt: LocalDateTime?) = apply { this.approvedAt = approvedAt }
        fun user(user: User) = apply { this.user = user }
        fun subscription(subscription: Subscription) = apply { this.subscription = subscription }

        fun build(): Payment {
            return Payment(
                orderId = orderId,
                paymentKey = paymentKey,
                orderName = orderName,
                totalAmount = totalAmount,
                method = method,
                status = status,
                approvedAt = approvedAt,
                user = user ?: throw IllegalArgumentException("User must not be null"),
                subscription = subscription ?: throw IllegalArgumentException("Subscription must not be null")
            )
        }
    }

}
