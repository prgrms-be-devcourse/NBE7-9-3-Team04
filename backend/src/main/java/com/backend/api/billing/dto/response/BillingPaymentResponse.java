package com.backend.api.billing.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record BillingPaymentResponse(
        @Schema(description = "상점 아이디", example = "tosspayments")
        String mId,

        @Schema(description = "결제 키", example = "y05n91dEvLex6BJGQOVDpgDQ0gDv0QVW4w2zNbgaYRMPoqmD")
        String paymentKey,

        @Schema(description = "주문 ID", example = "a4CWyWY5m89PNh7xJwhk1")
        String orderId,

        @Schema(description = "주문명", example = "프리미엄 구독 결제")
        String orderName,

        @Schema(description = "결제 상태", example = "DONE")
        String status,

        @Schema(description = "결제 요청 시각", example = "2023-08-08T16:30:01+09:00")
        String requestedAt,

        @Schema(description = "결제 승인 시각", example = "2023-08-08T16:30:01+09:00")
        String approvedAt,

        @Schema(description = "결제 카드 정보")
        CardInfo card
) {
    public record CardInfo(
            @Schema(description = "카드 번호 (마스킹 처리)", example = "1234-56**-****-7890")
            String number,
            @Schema(description = "카드 발급사 이름", example = "국민카드")
            String issuerName,

            @Schema(description = "카드 구분 (신용/체크)", example = "신용")
            String cardType
    ) {}
}