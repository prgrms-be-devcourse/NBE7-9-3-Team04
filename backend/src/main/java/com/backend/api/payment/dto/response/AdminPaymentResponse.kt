package com.backend.api.payment.dto.response

import com.backend.domain.payment.entity.Payment
import io.swagger.v3.oas.annotations.media.Schema
import lombok.Builder
import java.time.format.DateTimeFormatter

data class AdminPaymentResponse(
    @field:Schema(description = "토스에 전달되는 주문 ID", example = "order_20251015_001")
    val orderId: String,

    @field:Schema(description = "사용자 이름", example = "김데브")
    val userName: String,

    @field:Schema(description = "사용자 이메일", example = "test@example.com")
    val userEmail: String,

    @field:Schema(description = "주문 상품명", example = "프리미엄 월간 구독")
    val orderName: String,

    @field:Schema(description = "총 결제 금액", example = "9900")
    val amount: Long,

    @field:Schema(description = "결제 수단", example = "카드")
    val method: String?,

    @field:Schema(description = "결제 상태", example = "DONE")
    val status: String,

    @field:Schema(description = "결제 승인 시각", example = "2025-10-15 14:30")
    val approvedAt: String
) {
    companion object {

        private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        fun from(payment: Payment): AdminPaymentResponse {
            return AdminPaymentResponse(
                orderId = payment.orderId,
                userName = payment.user.name,
                userEmail = payment.user.email,
                orderName = payment.orderName,
                amount = payment.totalAmount,
                method = payment.method,
                status = payment.status.name,
                approvedAt = payment.approvedAt?.format(formatter) ?: "-"
            )
        }
    }
}
