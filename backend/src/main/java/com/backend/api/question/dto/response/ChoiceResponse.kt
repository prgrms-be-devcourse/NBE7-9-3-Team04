package com.backend.api.question.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI 응답 값 서브 내용")
@JsonIgnoreProperties(ignoreUnknown = true)
public record ChoiceResponse(
        @Schema(description = "메시지 응답")
        @JsonProperty("message")
        MessageResponse message
) {
}
