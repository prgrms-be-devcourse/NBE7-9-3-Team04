package com.backend.api.answer.dto.response

import com.backend.domain.answer.entity.Answer
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.domain.Page

@JvmRecord
data class AnswerPageResponse<T>(
    @field:Schema(description = "답변 응답DTO 리스트")
    val answers: List<T>,
    @field:Schema(description = "현재 페이지 번호", example = "3")
    val currentPage: Int,
    @field:Schema(description = "전체 페이지 수", example = "10")
    val totalPages: Int,
    @field:Schema(description = "전체 답변 수", example = "95")
    val totalCount: Int,
    @field:Schema(description = "페이지당 답변 수", example = "10")
    val pageSize: Int
) {
    companion object {
        @JvmStatic
        fun <T> from(page: Page<Answer>, answers: List<T>) = AnswerPageResponse(
            answers = answers,
            currentPage = page.number + 1,
            totalPages = page.totalPages,
            totalCount = page.totalElements.toInt(),
            pageSize = page.size
        )
    }
}
