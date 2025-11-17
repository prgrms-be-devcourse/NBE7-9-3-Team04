package com.backend.api.billing.dto.request

import io.swagger.v3.oas.annotations.media.Schema


data class BillingRequest(

    @field:Schema(description = "구매자 ID", example = "aENcQAtPdYbTjGhtQnNVj")
    val customerKey: String?,

    @field:Schema(description = " 일회성 인증 키", example = "e_826EDB0730790E96F116FFF3799A65DE")
    val authKey: String?
)
