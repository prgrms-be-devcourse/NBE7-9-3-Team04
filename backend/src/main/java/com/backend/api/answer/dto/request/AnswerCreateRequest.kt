package com.backend.api.answer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AnswerCreateRequest(
    @NotBlank(message = "답변 내용을 입력해주세요.")
    @Size(min = 1, max = 1000, message = "답변 내용은 1자 이상 1000자 이하여야 합니다.")
    @Schema(description = "면접 답변 내용", example = "이것은 면접 답변입니다.")
    String content,
    @NotNull(message = "답변 공개 여부를 선택해주세요.")
    @Schema(description = "답변 공개 여부", example = "true")
    Boolean isPublic
) {}
