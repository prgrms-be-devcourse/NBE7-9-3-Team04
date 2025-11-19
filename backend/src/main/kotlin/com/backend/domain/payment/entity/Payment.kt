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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @ManyToOne(fetch = FetchType.LAZY) //한 구독에 결제는 여러번
    @JoinColumn(name = "subscription_id", nullable = false)
    var subscription: Subscription ?= null

) : BaseEntity()