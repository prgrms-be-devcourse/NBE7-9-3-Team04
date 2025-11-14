package com.backend.api.post.dto.response;

import com.backend.domain.post.entity.PinStatus;
import com.backend.domain.post.entity.Post;
import com.backend.domain.post.entity.PostCategoryType;
import com.backend.domain.post.entity.PostStatus;
import com.backend.global.exception.ErrorCode;
import com.backend.global.exception.ErrorException;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Optional;

// 게시글 응답
    public record PostResponse(
        @Schema(description = "게시글 ID", example = "1")
            Long postId,

        @Schema(description = "제목", example = "첫번째 게시물")
            String title,

        @Schema(description = "한 줄 소개", example = "사이드 프로젝트 할 팀원 구합니다")
            String introduction,

        @Schema(description = "내용", example = "백엔드 2명, 프론트엔드 2명 구합니다. 주제는")
            String content,

        @Schema(description = "마감일", example = "2025-10-25T17:00:00")
            LocalDateTime deadline,

        @Schema(description = "작성일", example = "2025-10-18T10:30:00")
            LocalDateTime createDate,

        @Schema(description = "수정일", example = "2025-10-18T10:50:00")
            LocalDateTime modifyDate,

        @Schema(description = "진행 상태", example = "ING", allowableValues = {"ING", "CLOSED"})
            PostStatus status,

        @Schema(description = "상단 고정 여부", example = "NOT_PINNED", allowableValues = {"PINNED", "NOT_PINNED"})
            PinStatus pinStatus,

        @Schema(description = "모집 인원", example = "4")
        Integer recruitCount,

        @Schema(description = "작성자의 닉네임", example = "개발자A")
            String nickName, // 작성자의 닉네임

        @Schema(description = "카테고리 타입", example = "PROJECT")
        PostCategoryType categoryType,

        @Schema(description = "내 게시글 여부", example = "true")
        Boolean isMine
    ) {

    public static PostResponse from(Post post, Boolean isMine) {

        String nickName = Optional.ofNullable(post.getUsers())
                .map(user -> user.getNickname())
                .filter(name -> !name.isEmpty()) // 닉네임이 비어있는 문자열("")인 경우도
                .orElseThrow(() ->
                        new ErrorException(ErrorCode.NOT_FOUND_NICKNAME)
                );


        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getIntroduction(),
                post.getContent(),
                post.getDeadline(),
                post.getCreateDate(),
                post.getModifyDate(),
                post.getStatus(),
                post.getPinStatus(),
                post.getRecruitCount(),
                nickName,
                post.getPostCategoryType(),
                isMine
        );
    }
}