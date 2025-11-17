package com.backend.api.payment.dto.response

import com.backend.domain.payment.entity.Payment
import com.backend.domain.payment.entity.PaymentStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

//백 -> 클
data class PaymentResponse(

    @field:Schema(description = "Payment 엔티티의 고유 ID", example = "1")
    val paymentId: Long,

    @field:Schema(description = "토스에 전달되는 주문 ID", example = "1")
    val orderId: String,

    @field:Schema(description = "결제 키 값", example = "1111")
    val paymentKey: String?,

    @field:Schema(description = "주문 상품명", example = "PREMIUM")
    val orderName: String,

    @field:Schema(description = "총 결제 금액", example = "9900")
    val totalAmount: Long,

    @field:Schema(description = "결제 수단", example = "카드")
    val method: String?,

    @field:Schema(description = "결제 상태", example = "DONE")
    val status: PaymentStatus,

    @field:Schema(description = "결제 요청 시간", example = "2025-10-13T11:00:00")
    val requestedAt: LocalDateTime?,

    @field:Schema(description = "결제 수정 시간", example = "2025-10-13T12:00:00")
    val approvedAt: LocalDateTime?,

    @field:Schema(description = "연결된 구독 ID", example = "5")
    val subscriptionId: Long?,

    @field:Schema(description = "사용자 이메일", example = "test@naver.com")
    val userEmail: String?
) {
    companion object {
        fun from(payment: Payment): PaymentResponse {
            return PaymentResponse(
                payment.id,
                payment.orderId,
                payment.paymentKey,
                payment.orderName,
                payment.totalAmount,
                payment.method,
                payment.status,
                payment.createDate,
                payment.approvedAt,
                payment.subscription?.id,
                payment.user.email
            )
        }
    }
}
