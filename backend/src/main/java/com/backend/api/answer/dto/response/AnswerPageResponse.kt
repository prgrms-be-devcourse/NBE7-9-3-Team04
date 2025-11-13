package com.backend.api.answer.dto.response;

import com.backend.domain.answer.entity.Answer;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

public record AnswerPageResponse<T>(
        @Schema(description = "답변 응답DTO 리스트")
        List<T> answers,
        @Schema(description = "현재 페이지 번호", example = "3")
        int currentPage,
        @Schema(description = "전체 페이지 수", example = "10")
        int totalPages,
        @Schema(description = "전체 답변 수", example = "95")
        int totalCount,
        @Schema(description = "페이지당 답변 수", example = "10")
        int pageSize
) {
    public AnswerPageResponse(Page<Answer> page, List<T> answers) {
        this(
                answers,
                page.getNumber() + 1,
                page.getTotalPages(),
                (int) page.getTotalElements(),
                page.getSize()
        );
    }
}
