package com.backend.api.question.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI 응답 값 서브 내용")
@JsonIgnoreProperties(ignoreUnknown = true)
public record MessageResponse(
        @Schema(description = "프롬프트 규칙")
        @JsonProperty("role")
        String role,
        @Schema(description = "프롬프트 내용")
        @JsonProperty("content")
        String content
) {
}
