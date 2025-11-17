package com.backend.api.subscription.dto.response

import com.backend.domain.subscription.entity.Subscription
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime

data class SubscriptionResponse(
    @field:Schema(description = "구독 ID", example = "1")
    val id: Long,

    @field:Schema(description = "구독 이름", example = "PREMIUM")
    val subscriptionName: String,

    @field:Schema(description = "구독 유형", example = "PREMIUM")
    val subscriptionType: String,

    @field:Schema(description = "구독 활성화 여부", example = "true")
    val isActive: Boolean,

    @field:Schema(description = "구독 가격", example = "9900")
    val price: Long,

    @field:Schema(description = "구독 시작 날짜", example = "2025-10-23T03:00:00")
    val startDate: LocalDateTime,

    @field:Schema(description = "구독 만료 날짜", example = "2025-11-23T03:00:00")
    val endDate: LocalDateTime?,

    @field:Schema(description = "다음 결제 예정일", example = "2025-11-22")
    val nextBillingDate: LocalDate?,

    @field:Schema(description = "구매자 ID", example = "aENcQAtPdYbTjGhtQnNVj")
    val customerKey: String?,

    @field:Schema(description = "빌링키 (자동결제용 키)", example = "Z_t5vOvQxrj4499PeiJcjen28-V2RyqgYTwN44Rdzk0=")
    val billingKey: String?,

    @field:Schema(description = "사용자 ID", example = "3")
    val userId: Long

) {
    companion object {
        fun from(subscription: Subscription): SubscriptionResponse {
            return SubscriptionResponse(
                subscription.id,
                subscription.subscriptionName,
                subscription.subscriptionType.name,
                subscription.active,
                subscription.price,
                subscription.startDate,
                subscription.endDate,
                subscription.nextBillingDate,
                subscription.customerKey,
                subscription.billingKey,
                subscription.user.id
            )
        }
    }
}
