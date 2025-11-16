package com.backend.api.payment.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@JvmRecord
data class PaymentCancelResponse(

    @field:Schema(description = "결제 키 값", example = "1111")
    val paymentKey: String?,

    @field:Schema(description = "토스에 전달되는 주문 ID", example = "1")
    val orderId: String,

    @field:Schema(description = "결제 상태", example = "CANCELED")
    val status: String,

    @field:Schema(description = "결제 취소 정보 목록 (보통 1건)")
    val cancels: List<CancelInfo>

) {
    @Schema(description = "결제 취소 상세 정보 ")
    data class CancelInfo(

        @field:Schema(description = "취소 금액", example = "15000")
        val cancelAmount: Int,

        @field:Schema(description = "취소 사유", example = "DB transaction failed")
        val cancelReason: String,

        @field:Schema(description = "취소 처리 시각", example = "2025-01-01T12:32:00+09:00")
        val canceledAt: String
    )

}