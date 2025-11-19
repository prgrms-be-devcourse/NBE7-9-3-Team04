package com.backend.api.question.dto.request

import com.backend.domain.question.entity.QuestionCategoryType
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class QuestionUpdateRequest(

    @field:NotBlank(message = "질문 제목은 필수입니다.")
    @field:Schema(description = "질문 제목", example = "수정할 제목")
    val title: String,

    @field:NotBlank(message = "질문 내용은 필수입니다.")
    @field:Schema(description = "질문 내용", example = "수정할 내용")
    val content: String,

    @field:Schema(description = "수정할 카테고리 타입", example = "SPRING")
    val categoryType: QuestionCategoryType
)
