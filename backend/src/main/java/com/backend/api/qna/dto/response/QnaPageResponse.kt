package com.backend.api.qna.dto.response

import com.backend.domain.qna.entity.Qna
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.domain.Page

data class QnaPageResponse<T>(

    @field:Schema(description = "Qna 응답DTO 리스트")
    val qna: List<T>,

    @field:Schema(description = "현재 페이지 번호", example = "3")
    val currentPage: Int,

    @field:Schema(description = "전체 페이지 수", example = "10")
    val totalPages: Int,

    @field:Schema(description = "전체 Qna 수", example = "95")
    val totalCount: Int,

    @field:Schema(description = "페이지당 Qna 수", example = "10")
    val pageSize: Int
) {
    companion object {
        fun <T> from(page: Page<Qna>, qna: List<T>): QnaPageResponse<T> {
            return QnaPageResponse(
                qna = qna,
                currentPage = page.number + 1,
                totalPages = page.totalPages,
                totalCount = page.totalElements.toInt(),
                pageSize = page.size
            )
        }
    }
}
