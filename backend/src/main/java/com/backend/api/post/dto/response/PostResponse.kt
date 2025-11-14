package com.backend.api.post.dto.response

import com.backend.domain.post.entity.PinStatus
import com.backend.domain.post.entity.Post
import com.backend.domain.post.entity.PostCategoryType
import com.backend.domain.post.entity.PostStatus
import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@JvmRecord
data class PostResponse(
    @Schema(description = "게시글 ID", example = "1")
    val postId: Long,

    @Schema(description = "제목", example = "첫번째 게시물")
    val title: String,

    @Schema(description = "한 줄 소개", example = "사이드 프로젝트 할 팀원 구합니다")
    val introduction: String,

    @Schema(description = "내용", example = "백엔드 2명, 프론트엔드 2명 구합니다. 주제는")
    val content: String,

    @Schema(description = "마감일", example = "2025-10-25T17:00:00")
    val deadline: LocalDateTime,

    @Schema(description = "작성일", example = "2025-10-18T10:30:00")
    val createDate: LocalDateTime,

    @Schema(description = "수정일", example = "2025-10-18T10:50:00")
    val modifyDate: LocalDateTime,

    @Schema(description = "진행 상태", example = "ING", allowableValues = ["ING", "CLOSED"])
    val status: PostStatus,

    @Schema(description = "상단 고정 여부", example = "NOT_PINNED", allowableValues = ["PINNED", "NOT_PINNED"])
    val pinStatus: PinStatus,

    @Schema(description = "모집 인원", example = "4")
    val recruitCount: Int,

    @Schema(description = "작성자의 닉네임", example = "개발자A")
    val nickName: String,

    @Schema(description = "카테고리 타입", example = "PROJECT")
    val categoryType: PostCategoryType,

    @Schema(description = "내 게시글 여부", example = "true")
    val isMine: Boolean
) {
    companion object {
        @JvmStatic
        fun from(post: Post, isMine: Boolean): PostResponse {

            val nickName = post.users.nickname
                ?.takeIf { it.isNotEmpty() }
                ?: throw ErrorException(ErrorCode.NOT_FOUND_NICKNAME)

            return PostResponse(
                postId = post.id,
                title = post.title,
                introduction = post.introduction,
                content = post.content,
                deadline = post.deadline,
                createDate = post.createDate,
                modifyDate = post.modifyDate,
                status = post.status,
                pinStatus = post.pinStatus,
                recruitCount = post.recruitCount,
                nickName = nickName,
                categoryType = post.postCategoryType,
                isMine = isMine
            )
        }
    }
}