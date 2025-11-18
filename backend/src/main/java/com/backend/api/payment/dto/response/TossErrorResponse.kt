package com.backend.api.payment.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class TossErrorResponse(

    @field:Schema(description = "에러 코드", example = "400")
    val code: String,

    @field:Schema(description = "에러 메시지", example = "오류입니다.")
    val message: String
)