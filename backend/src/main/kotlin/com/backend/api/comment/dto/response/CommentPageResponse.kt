package com.backend.api.comment.dto.response

import com.backend.domain.comment.entity.Comment
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.domain.Page

data class CommentPageResponse<T>(

    @field:Schema(description = "댓글 응답DTO 리스트")
    val comments: List<T>,

    @field:Schema(description = "현재 페이지 번호", example = "3")
    val currentPage: Int,

    @field:Schema(description = "전체 페이지 수", example = "10")
    val totalPages: Int,

    @field:Schema(description = "전체 댓글 수", example = "95")
    val totalCount: Int,

    @field:Schema(description = "페이지당 댓글 수", example = "10")
    val pageSize: Int
) {
    companion object {
        fun <T> from(page: Page<Comment>, comments: List<T>): CommentPageResponse<T> {
            return CommentPageResponse(
                comments = comments,
                currentPage = page.number + 1,
                totalPages = page.totalPages,
                totalCount = page.totalElements.toInt(),
                pageSize = page.size
            )
        }
    }
}
