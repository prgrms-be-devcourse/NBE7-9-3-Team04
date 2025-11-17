package com.backend.api.question.dto.response

import com.backend.domain.question.entity.Question
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.domain.Page

data class QuestionPageResponse<T>(

    @field:Schema(description = "질문 응답DTO 리스트")
    val questions: List<T>,

    @field:Schema(description = "현재 페이지 번호", example = "3")
    val currentPage: Int,

    @field:Schema(description = "전체 페이지 수", example = "10")
    val totalPages: Int,

    @field:Schema(description = "전체 질문 수", example = "95")
    val totalCount: Int,

    @field:Schema(description = "페이지당 질문 수", example = "10")
    val pageSize: Int
) {

    companion object {
        fun <T> from(page: Page<Question>, questions: List<T>): QuestionPageResponse<T> =
            QuestionPageResponse(
                questions = questions,
                currentPage = page.number + 1,
                totalPages = page.totalPages,
                totalCount = page.totalElements.toInt(),
                pageSize = page.size
            )
    }
}
