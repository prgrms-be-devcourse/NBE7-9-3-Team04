package com.backend.api.question.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
@Schema(description = "AI 응답 값")
@JsonIgnoreProperties(ignoreUnknown = true)
public record ChatGptResponse(
        @Schema(description = "전체 값")
        @JsonProperty("choices")
        List<ChoiceResponse> choiceResponses
) {
}

