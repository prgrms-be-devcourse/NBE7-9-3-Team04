package com.backend.api.qna.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class QnaAnswerRequest(

    @field:NotBlank(message = "Qna 답변은 필수입니다.")
    @field:Schema(description = "Qna 답변", example = "답변 내용입니다.")
    val answer: String
)
