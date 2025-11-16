package com.backend.api.question.dto.response

import com.backend.domain.question.entity.Question
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "AI 면접 질문 생성 응답")

data class AiQuestionResponse(
    @field:JsonProperty("title") @field:Schema(
        description = "제목"
    ) @param:Schema(description = "제목") @param:JsonProperty(
        "title"
    ) val title: String,
    @field:JsonProperty("content") @field:Schema(
        description = "내용"
    ) @param:Schema(description = "내용") @param:JsonProperty(
        "content"
    ) val content: String,
    @field:JsonProperty("score") @field:Schema(
        description = "점수"
    ) @param:Schema(description = "점수") @param:JsonProperty(
        "score"
    ) val score: Int
) {
    companion object {
        fun toDtoList(questions: List<Question>): List<AiQuestionResponse> {
            return questions.stream()
                .map{ question: Question -> toDto(question) }
                .toList()
        }

        fun toDto(question: Question): AiQuestionResponse {
            return AiQuestionResponse(
                question.title,
                question.content,
                question.score
            )
        }
    }
}
