package com.backend.domain.payment.entity

enum class PaymentStatus {
    REQUESTED,  // 주문 생성 (초기 상태)
    READY,  // 가상계좌 발급 등
    IN_PROGRESS,  // 결제 진행 중 (토스 응답에 나옴)
    DONE,  // 결제 완료 (최종 승인 성공)
    CANCELED,  // 결제 취소/환불 완료
    FAILED,  // 결제 실패
    PARTIAL_CANCELED // 부분 취소 완료
}