package com.backend.api.comment.dto.response

import com.backend.domain.comment.entity.Comment
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class CommentMypageResponse(

    @field:Schema(description = "댓글 ID", example = "1")
    val id: Long,

    @field:Schema(description = "작성일", example = "2025-10-13T11:00:00")
    val createDate: LocalDateTime,

    @field:Schema(description = "수정일", example = "2025-10-13T12:00:00")
    val modifyDate: LocalDateTime,

    @field:Schema(description = "댓글 내용", example = "이것은 댓글입니다.")
    val content: String,

    @field:Schema(description = "작성자 ID", example = "1")
    val authorId: Long,

    @field:Schema(description = "작성자 닉네임", example = "user123")
    val authorNickName: String,

    @field:Schema(description = "게시글 ID", example = "1")
    val postId: Long,

    @field:Schema(description = "게시글 제목", example = "게시글 제목입니다.")
    val postTitle: String
) {
    companion object {
        fun from(comment: Comment): CommentMypageResponse {
            return CommentMypageResponse(
                id = comment.id,
                createDate = comment.createDate,
                modifyDate = comment.modifyDate,
                content = comment.content,
                authorId = comment.author.id,
                authorNickName = comment.author.nickname,
                postId = comment.post.id,
                postTitle = comment.post.title
            )
        }
    }
}
