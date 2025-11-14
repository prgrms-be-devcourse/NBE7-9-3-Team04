package com.backend.api.post.dto.response

import com.backend.domain.post.entity.Post
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.domain.Page

@JvmRecord
data class PostPageResponse<T>(
    @Schema(description = "게시물 응답DTO 리스트")
    val posts: List<T>,

    @Schema(description = "현재 페이지 번호", example = "3")
    val currentPage: Int,

    @Schema(description = "전체 페이지 수", example = "10")
    val totalPages: Int,

    @Schema(description = "전체 게시글 수", example = "95")
    val totalCount: Int,

    @Schema(description = "페이지당 게시글 수", example = "10")
    val pageSize: Int
) {
    companion object {
        fun <T> from(page: Page<Post>, posts: List<T>): PostPageResponse<T> {
            return PostPageResponse(
                posts = posts,
                currentPage = page.number + 1,
                totalPages = page.totalPages,
                totalCount = page.totalElements.toInt(),
                pageSize = page.size
            )
        }
    }
}