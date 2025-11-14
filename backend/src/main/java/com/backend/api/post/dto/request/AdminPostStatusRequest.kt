package com.backend.api.post.dto.request

import com.backend.domain.post.entity.PostStatus
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull

@JvmRecord
data class AdminPostStatusRequest(
    @Schema(description = "변경할 게시글 상태", example = "CLOSED")
    @field:NotNull(message = "게시글 상태는 필수입니다.")
    val status: PostStatus?
)



