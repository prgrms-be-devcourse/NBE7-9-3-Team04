package com.backend.api.payment.dto.request

import io.swagger.v3.oas.annotations.media.Schema

data class AutoPaymentRequest(
    @field:Schema(description = "구매자 ID", example = "aENcQAtPdYbTjGhtQnNVj")
    val customerKey: String,

    @field:Schema(description = "빌링키 (자동결제용 키)", example = "Z_t5vOvQxrj4499PeiJcjen28-V2RyqgYTwN44Rdzk0=")
    val billingKey: String,

    @field:Schema(description = "결제 금액", example = "9900")
    val amount: Long,

    @field:Schema(description = "주문 ID (고유값)", example = "b05c8d5b-7414-44af-9bcd-053e5eeec1e1")
    val orderId: String,

    @field:Schema(description = "주문명", example = "프리미엄 구독 자동결제")
    val orderName: String,

    @field:Schema(description = "구매자 이메일", example = "user@email.com")
    val customerEmail: String,

    @field:Schema(description = "구매자 이름", example = "홍길동")
    val customerName: String
)

