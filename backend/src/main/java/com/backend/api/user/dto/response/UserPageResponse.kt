package com.backend.api.user.dto.response

import com.backend.domain.user.entity.User
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.domain.Page

@JvmRecord
data class UserPageResponse<T>(
    @field:Schema(description = "유저 응답DTO 리스트")
     val users: List<T>,

    @field:Schema(description = "현재 페이지 번호", example = "3")
    val currentPage: Int,

    @field:Schema(description = "전체 페이지 수", example = "10")
    val totalPages: Int,

    @field:Schema(description = "전체 유저 수", example = "95")
    val totalCount: Int,

    @field:Schema(description = "페이지당 유저 수", example = "10")
    val pageSize: Int
) {
    companion object {
        @JvmStatic
        fun <T> from(page: Page<User>, users: List<T>): UserPageResponse<T> {
            return UserPageResponse(
                users,
                page.number + 1,
                page.totalPages,
                page.totalElements.toInt(),
                page.size
            )
        }
    }
}
