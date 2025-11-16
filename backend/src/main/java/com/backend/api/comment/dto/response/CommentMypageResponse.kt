package com.backend.api.comment.dto.response;

import com.backend.domain.comment.entity.Comment;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record CommentMypageResponse(
        @Schema(description = "댓글 ID", example = "1")
        Long id,
        @Schema(description = "작성일", example = "2025-10-13T11:00:00")
        LocalDateTime createDate,
        @Schema(description = "수정일", example = "2025-10-13T12:00:00")
        LocalDateTime modifyDate,
        @Schema(description = "댓글 내용", example = "이것은 댓글입니다.")
        String content,
        @Schema(description = "작성자 ID", example = "1")
        Long authorId,
        @Schema(description = "작성자 닉네임", example = "user123")
        String authorNickName,
        @Schema(description = "게시글 ID", example = "1")
        Long postId,
        @Schema(description = "게시글 제목", example = "게시글 제목입니다.")
        String postTitle
) {
    public CommentMypageResponse(Comment comment) {
        this(
                comment.getId(),
                comment.getCreateDate(),
                comment.getModifyDate(),
                comment.getContent(),
                comment.getAuthor().getId(),
                comment.getAuthor().getNickname(),
                comment.getPost().getId(),
                comment.getPost().getTitle()
        );
    }
}