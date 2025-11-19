package com.backend.api.payment.dto.request

import io.swagger.v3.oas.annotations.media.Schema

data class PaymentCancelRequest(

    @field:Schema(description = "결제 취소 사유", example = "DB transaction failed")
    val cancelReason: String
)