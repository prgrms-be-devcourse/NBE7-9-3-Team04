package com.backend.api.question.dto.request

import com.backend.domain.question.entity.QuestionCategoryType
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

@Schema(description = "관리자 질문 수정 요청")
data class AdminQuestionUpdateRequest(

    @field:NotBlank(message = "질문 제목은 필수입니다.")
    @field:Schema(description = "질문 제목", example = "수정된 제목")
    val title: String,

    @field:NotBlank(message = "질문 내용은 필수입니다.")
    @field:Schema(description = "질문 내용", example = "수정된 내용")
    val content: String,

    @field:NotNull(message = "질문 승인 여부는 필수입니다.")
    @field:Schema(description = "질문 승인 여부", example = "true")
    val isApproved: Boolean,

    @field:Min(value = 0, message = "점수는 0 이상이어야 합니다.")
    @field:Max(value = 50, message = "점수는 50 이하이어야 합니다.")
    @field:Schema(description = "질문 점수", example = "10")
    val score: Int?,

    @field:NotNull(message = "카테고리 타입은 필수입니다.")
    @field:Schema(description = "수정할 카테고리 타입", example = "DATABASE")
    val categoryType: QuestionCategoryType
)
