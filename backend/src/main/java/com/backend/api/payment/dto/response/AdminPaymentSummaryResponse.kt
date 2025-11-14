package com.backend.api.payment.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@JvmRecord
data class AdminPaymentSummaryResponse(
    @field:Schema(description = "총 결제 건수", example = "5")
    val totalPayments: Long,

    @field:Schema(description = "성공한 결제 건수", example = "4")
    val successPayments: Long,

    @field:Schema(description = "총 수익 (원)", example = "39600")
    val totalRevenue: Long
)