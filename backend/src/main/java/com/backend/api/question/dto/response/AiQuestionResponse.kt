package com.backend.api.question.dto.response;

import com.backend.domain.question.entity.Question;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "AI 면접 질문 생성 응답")
public record AiQuestionResponse(
        @Schema(description = "제목")
        @JsonProperty("title")
        String title,
        @Schema(description = "내용")
        @JsonProperty("content")
        String content,
        @Schema(description = "점수")
        @JsonProperty("score")
        Integer score
) {
        public static List<AiQuestionResponse> toDtoList(List<Question> questions){
                return questions.stream()
                        .map(AiQuestionResponse::toDto)
                        .toList();
        }

        public static AiQuestionResponse toDto(Question question) {
                return new AiQuestionResponse(
                        question.getTitle(),
                        question.getContent(),
                        question.getScore()
                );
        }
}
