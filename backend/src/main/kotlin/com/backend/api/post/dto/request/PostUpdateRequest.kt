package com.backend.api.post.dto.request

import com.backend.domain.post.entity.PinStatus
import com.backend.domain.post.entity.PostCategoryType
import com.backend.domain.post.entity.PostStatus
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

//게시글 수정
data class PostUpdateRequest(
    @field:NotBlank(message = "제목은 필수입니다.")
    @field:Size(min = 2, max = 255, message = "제목은 2자 이상 255자 이하로 입력해주세요.")
    val title: String,

    @field:NotBlank(message = "한 줄 소개는 필수입니다.")
    @field:Size(min = 10, message = "한 줄 소개는 최소 10자 이상 입력해주세요.")
    val introduction: String,

    @field:NotBlank(message = "내용은 필수입니다.")
    @field:Size(min = 10, message = "내용은 최소 10자 이상 입력해주세요.")
    val content: String,

    @field:NotNull(message = "마감일은 필수입니다.")
    val deadline: LocalDateTime,

    @field:NotNull(message = "진행 상태는 필수입니다.")
    val status: PostStatus,

    @field:NotNull(message = "상단 고정 여부는 필수입니다.")
    val pinStatus: PinStatus,

    @field:NotNull(message = "모집 인원은 필수입니다.")
    @field:Min(value = 1, message = "모집 인원은 최소 1명 이상이어야 합니다.")
    val recruitCount: Int,

    @field:NotNull
    val categoryType: PostCategoryType
)