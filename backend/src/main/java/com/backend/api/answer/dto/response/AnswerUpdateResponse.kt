package com.backend.api.answer.dto.response;

import com.backend.domain.answer.entity.Answer;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record AnswerUpdateResponse(
        @Schema(description = "면접 답변 ID", example = "1")
        Long id,
        @Schema(description = "작성일", example = "2025-10-13T11:00:00")
        LocalDateTime createDate,
        @Schema(description = "수정일", example = "2025-10-13T12:00:00")
        LocalDateTime modifyDate,
        @Schema(description = "면접 답변 내용", example = "이것은 면접 답변입니다.")
        String content,
        @Schema(description = "답변 공개 여부", example = "true")
        boolean isPublic,
        @Schema(description = "작성자 ID", example = "1")
        Long authorId,
        @Schema(description = "작성자 닉네임", example = "user123")
        String authorNickName,
        @Schema(description = "질문 ID", example = "1")
        Long questionId
) {
    public AnswerUpdateResponse(Answer answer) {
        this(
                answer.getId(),
                answer.getCreateDate(),
                answer.getModifyDate(),
                answer.getContent(),
                answer.isPublic(),
                answer.getAuthor().getId(),
                answer.getAuthor().getNickname(),
                answer.getQuestion().getId()
        );
    }
}
