package com.backend.api.comment.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CommentRequest(

    @field:NotBlank(message = "댓글 내용을 입력해주세요.")
    @field:Size(min = 1, max = 500, message = "댓글 내용은 1자 이상 500자 이하로 입력해주세요.")
    @field:Schema(description = "댓글 내용", example = "이것은 댓글입니다.")
    val content: String?

)
