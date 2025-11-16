package com.backend.api.question.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record MessagesRequest(
        @Schema(description = "프롬프트 작성 규칙")
        String role,
        @Schema(description = "프롬프트 내용")
        String content
) {
    public static MessagesRequest of(String role ,String content){
        return new MessagesRequest(role,content);
    }
}
