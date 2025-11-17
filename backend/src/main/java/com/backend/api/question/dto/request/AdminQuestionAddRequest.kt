package com.backend.api.question.dto.request

import com.backend.domain.question.entity.QuestionCategoryType
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.*

data class AdminQuestionAddRequest(

    @field:NotBlank(message = "질문 제목은 필수입니다.")
    @field:Schema(description = "질문 제목", example = "Spring Bean의 생명주기는 어떻게 되나요?")
    val title: String,

    @field:NotBlank(message = "질문 내용은 필수입니다.")
    @field:Schema(description = "질문 내용", example = "Bean이 생성되고 초기화되고 소멸되는 과정에 대해 설명해주세요.")
    val content: String,

    @field:NotNull(message = "질문 카테고리 타입은 필수입니다.")
    @field:Schema(description = "질문 카테고리 타입", example = "SPRING")
    val categoryType: QuestionCategoryType,

    @field:NotNull(message = "승인 여부는 필수입니다.")
    @field:Schema(description = "승인 여부", example = "true")
    val isApproved: Boolean,

    @field:NotNull(message = "점수는 필수입니다.")
    @field:Min(value = 0, message = "점수는 0 이상이어야 합니다.")
    @field:Max(value = 50, message = "점수는 50 이하이어야 합니다.")
    @field:Schema(description = "초기 점수 (0~50점 사이)", example = "10")
    val score: Int
)
