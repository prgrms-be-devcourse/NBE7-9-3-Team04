package com.backend.api.question.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class QuestionScoreRequest(

    @field:Min(value = 0, message = "점수는 0 이상이어야 합니다.")
    @field:Max(value = 50, message = "점수는 50 이하이어야 합니다.")
    @field:Schema(description = "질문 점수", example = "5")
    val score: Int?
)
