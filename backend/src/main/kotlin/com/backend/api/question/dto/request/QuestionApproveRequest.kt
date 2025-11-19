package com.backend.api.question.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull

data class QuestionApproveRequest(

    @field:NotNull(message = "승인 여부는 필수입니다.")
    @field:Schema(description = "승인 여부", example = "true")
    val isApproved: Boolean
)
