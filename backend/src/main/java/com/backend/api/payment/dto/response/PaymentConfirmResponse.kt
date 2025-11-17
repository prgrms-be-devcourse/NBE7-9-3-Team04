package com.backend.api.payment.dto.response

import com.backend.domain.payment.entity.PaymentStatus
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.swagger.v3.oas.annotations.media.Schema

//Toss Payments API가 정한 필드와 형식을 그대로 매핑
//토스 API -> 백
@JsonIgnoreProperties(ignoreUnknown = true)
data class PaymentConfirmResponse(

    @field:Schema(description = "토스에 전달되는 주문 ID", example = "1")
    val orderId: String,

    @field:Schema(description = "결제 키 값", example = "1111")
    val paymentKey: String,

    @field:Schema(description = "주문 상품명", example = "PREMIUM")
    val orderName: String,

    @field:Schema(description = "총 결제 금액", example = "9900")
    val totalAmount: Long,

    @field:Schema(description = "결제 수단", example = "카드")
    val method: String,

    @field:Schema(description = "결제 상태", example = "DONE")
    val status: PaymentStatus
)
