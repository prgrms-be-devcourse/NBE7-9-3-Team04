package com.backend.api.billing.dto.response

import com.backend.domain.subscription.entity.Subscription
import io.swagger.v3.oas.annotations.media.Schema

data class BillingResponse(
    @field:Schema(description = "빌링키 (자동결제용 키)", example = "Z_t5vOvQxrj4499PeiJcjen28-V2RyqgYTwN44Rdzk0=")
    val billingKey: String,

    @field:Schema(description = "구매자 ID", example = "aENcQAtPdYbTjGhtQnNVj")
    val customerKey: String,
) {

    companion object {
        fun from(subscription: Subscription): BillingResponse {
            return BillingResponse(
                billingKey = subscription.billingKey ?: "",
                customerKey = subscription.customerKey
            )
        }
    }
}
